package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.entity.Degerlendirme;
import com.example.StajEnvanterProjesi.entity.Sorumlu;
import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.StajGorev;
import com.example.StajEnvanterProjesi.entity.StajGorevMaddesi;
import com.example.StajEnvanterProjesi.entity.Stajyer;
import com.example.StajEnvanterProjesi.entity.dto.Bildirim.YaklasanBitisResponse;
import com.example.StajEnvanterProjesi.entity.dto.SayfaResponse;
import com.example.StajEnvanterProjesi.entity.enums.GorevDurumu;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.repository.StajGorevRepository;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.service.BildirimService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BildirimServiceImpl implements BildirimService {

    private static final Logger log = LoggerFactory.getLogger(BildirimServiceImpl.class);

    private static final DateTimeFormatter TARIH = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StajRepository stajRepository;
    // Degerlendirme maili icin: gorev ilerleme ozeti + puanlayan sorumlunun adi.
    private final StajGorevRepository gorevRepository;
    private final SorumluRepository sorumluRepository;

    /**
     * Mail altyapisi ZORUNLU DEGIL. spring.mail.host tanimli degilse Spring
     * JavaMailSender bean'ini hic olusturmaz; ObjectProvider ile opsiyonel
     * baglayip yoksa log'a dusuyoruz. Boylece SMTP'siz bir ortamda job
     * patlamiyor, sadece gonderemedigi maili yaziyor.
     */
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.bildirim.gun:5}")
    private int varsayilanGun;

    @Value("${app.bildirim.mail.gonder:false}")
    private boolean mailGonder;

    @Value("${app.bildirim.mail.kimden:staj-envanter@localhost}")
    private String kimden;

    public BildirimServiceImpl(StajRepository stajRepository,
                               StajGorevRepository gorevRepository,
                               SorumluRepository sorumluRepository,
                               ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.stajRepository = stajRepository;
        this.gorevRepository = gorevRepository;
        this.sorumluRepository = sorumluRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    // ---- Sorgular ----

    @Override
    public List<YaklasanBitisResponse> yaklasanBitisler(int gun, String q) {
        return bitisiYaklasanStajlar(gun, q).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<YaklasanBitisResponse> benimYaklasanBitislerim(Long sorumluId, int gun, String q) {
        // Sorumlu asil VEYA yedek olabilir; ikisinde de haberdar olmali.
        return bitisiYaklasanStajlar(gun, q).stream()
                .filter(s -> ilgili(s, sorumluId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- Zamanlanmis is ----

    @Override
    public int bitisiYaklasanlariBildir() {
        // Mail isi hicbir zaman suzulmez: arama ekran icin, bildirim herkese gider.
        List<Staj> stajlar = bitisiYaklasanStajlar(varsayilanGun, null);
        if (stajlar.isEmpty()) {
            log.info("Bitisi yaklasan staj yok, bildirim gonderilmedi.");
            return 0;
        }

        // Sorumlu bazinda grupla: bir sorumluya TEK mail gider, icinde tum stajyerleri.
        // Ayni staj hem asil hem yedek icin listeye girer (ikisi de haberdar olmali).
        //
        // NOT: anahtar olarak Sorumlu DEGIL, sorumlu ID'si kullaniliyor. Sorumlu @Data ile
        // isaretli, yani equals/hashCode tum alanlardan uretiliyor — icinde lazy @ManyToOne
        // departman da var. Hibernate proxy'si ya da lazy yukleme hashCode'u degistirebilir
        // ve entity HashMap anahtari olarak guvenilmez olur.
        Map<Long, List<Staj>> stajlarBySorumluId = new LinkedHashMap<>();
        Map<Long, Sorumlu> sorumlular = new LinkedHashMap<>();
        for (Staj s : stajlar) {
            // Stream.of: yedek null olabilir, List.of null kabul etmez (NPE).
            Stream.of(s.getAsilSorumlu(), s.getYedekSorumlu())
                    .filter(Objects::nonNull)
                    .forEach(sorumlu -> {
                        sorumlular.putIfAbsent(sorumlu.getId(), sorumlu);
                        stajlarBySorumluId
                                .computeIfAbsent(sorumlu.getId(), k -> new ArrayList<>())
                                .add(s);
                    });
        }

        int gonderilen = 0;
        for (Map.Entry<Long, List<Staj>> e : stajlarBySorumluId.entrySet()) {
            if (gonder(sorumlular.get(e.getKey()), e.getValue())) {
                gonderilen++;
            }
        }
        log.info("Bitis bildirimi: {} staj, {} sorumluya gonderildi.", stajlar.size(), gonderilen);
        return gonderilen;
    }

    // ---- yardimcilar ----

    private List<Staj> bitisiYaklasanStajlar(int gun, String q) {
        LocalDate bugun = LocalDate.now();
        // Yalnizca DEVAM_EDIYOR: tamamlanmis/iptal stajin bitisi hatirlatilmaz.
        return stajRepository.bitisiYaklasanlar(
                StajDurumu.DEVAM_EDIYOR, bugun, bugun.plusDays(gun), SayfaResponse.benzerlik(q));
    }

    private boolean ilgili(Staj s, Long sorumluId) {
        boolean asil = s.getAsilSorumlu() != null && s.getAsilSorumlu().getId().equals(sorumluId);
        boolean yedek = s.getYedekSorumlu() != null && s.getYedekSorumlu().getId().equals(sorumluId);
        return asil || yedek;
    }

    /** @return mail gercekten gonderildiyse true; adres yoksa ya da gonderim kapaliysa false */
    private boolean gonder(Sorumlu sorumlu, List<Staj> stajlar) {
        if (sorumlu.getEmail() == null || sorumlu.getEmail().isBlank()) {
            log.warn("Sorumlu {} {} icin e-posta adresi yok, bildirim atlandi.",
                    sorumlu.getAd(), sorumlu.getSoyad());
            return false;
        }
        return gonderMail(sorumlu.getEmail(),
                "Bitisi yaklasan stajlar (" + stajlar.size() + ")",
                govdeOlustur(sorumlu, stajlar));
    }

    /**
     * Ortak dusuk seviye gonderim. Adres/SMTP/gonderim bayragi kontrolu TEK yerde;
     * bir alicinin patlamasi cagiranin akisini bozmaz (istisna burada yutulur).
     *
     * @return mail gercekten gonderildiyse true
     */
    private boolean gonderMail(String alici, String konu, String govde) {
        if (alici == null || alici.isBlank()) {
            log.warn("Alici e-posta adresi yok, mail atlandi. Konu: {}", konu);
            return false;
        }

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (!mailGonder || sender == null) {
            // SMTP yoksa ya da gonderim kapaliysa: sessizce yutma, log'a yaz.
            log.info("[MAIL GONDERILMEDI - gonderim kapali ya da SMTP yok] Alici: {} | Konu: {}\n{}",
                    alici, konu, govde);
            return false;
        }

        try {
            // MimeMessage + UTF-8: konu ve govdedeki Turkce karakterler (s/c/g/i/o/u)
            // dogru kodlanir. SimpleMailMessage varsayilan kodlamada bozabiliyordu.
            MimeMessage mesaj = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mesaj, false, "UTF-8");
            helper.setFrom(kimden);
            helper.setTo(alici);
            helper.setSubject(konu);
            helper.setText(govde, false); // duz metin (HTML degil)
            sender.send(mesaj);
            return true;
        } catch (Exception ex) {
            log.error("Mail gonderilemedi: {} -> {}", alici, ex.getMessage());
            return false;
        }
    }

    // ---- Degerlendirme (kapanis) maili ----

    @Override
    public boolean degerlendirmeTamamlandiBildir(Staj staj, Degerlendirme degerlendirme) {
        Stajyer stajyer = staj.getStajyer();
        if (stajyer == null) {
            log.warn("Staj {} icin stajyer yok, degerlendirme maili atlandi.", staj.getHashId());
            return false;
        }

        String konu = "Staj Değerlendirmeniz Tamamlandı — "
                + ad(staj.getDepartman() != null ? staj.getDepartman().getAd() : null)
                + " / " + ad(staj.getDonem() != null ? staj.getDonem().getAd() : null);

        boolean sonuc = gonderMail(stajyer.getEmail(), konu, degerlendirmeGovdesi(staj, degerlendirme));
        log.info("Degerlendirme maili {} (staj {}, stajyer {}).",
                sonuc ? "gonderildi" : "gonderilmedi/atlandi", staj.getHashId(), stajyer.getEmail());
        return sonuc;
    }

    private String degerlendirmeGovdesi(Staj staj, Degerlendirme d) {
        Stajyer st = staj.getStajyer();
        StringBuilder sb = new StringBuilder();
        sb.append("Merhaba ").append(st.getAd()).append(" ").append(st.getSoyad()).append(",\n\n");
        sb.append(ad(staj.getDepartman() != null ? staj.getDepartman().getAd() : null))
          .append(" departmanı / ")
          .append(ad(staj.getDonem() != null ? staj.getDonem().getAd() : null))
          .append(" dönemi stajınız değerlendirilmiştir.\n\n");

        sb.append("  Staj tarihleri  : ").append(tarih(staj.getStajBaslangicTarihi()))
          .append(" - ").append(tarih(staj.getStajBitisTarihi())).append("\n");
        sb.append("  Puanınız        : ").append(puanFmt(d.getPuan())).append(" / 100\n");
        sb.append("  Değerlendiren   : ").append(degerlendirenAdi(d, staj)).append("\n");
        sb.append("  Tarih           : ").append(tarih(d.getCreateDate())).append("\n");
        sb.append("  Durum           : ").append(staj.getDurum()).append("\n");

        int[] ozet = gorevOzeti(staj); // [tamamlanan, toplam]
        if (ozet[1] > 0) {
            int yuzde = (int) Math.round(ozet[0] * 100.0 / ozet[1]);
            sb.append("  Görev ilerlemesi: ").append(ozet[0]).append("/").append(ozet[1])
              .append(" madde tamamlandı (%").append(yuzde).append(")\n");
        }

        // Yorum boş ise blok hiç yazılmaz (çirkin boş tırnak görünmesin).
        if (d.getYorum() != null && !d.getYorum().isBlank()) {
            sb.append("\nSorumlu yorumu:\n\"").append(d.getYorum().trim()).append("\"\n");
        }

        sb.append("\nBu e-posta otomatik olarak gönderilmiştir.\n");
        return sb.toString();
    }

    /** Puanlayan asil sorumlunun adi; iz kaydi yoksa stajin asil sorumlusuna duser. */
    private String degerlendirenAdi(Degerlendirme d, Staj staj) {
        if (d.getPuanlayanSorumluId() != null) {
            Sorumlu s = sorumluRepository.findById(d.getPuanlayanSorumluId()).orElse(null);
            if (s != null) {
                return s.getAd() + " " + s.getSoyad();
            }
        }
        Sorumlu asil = staj.getAsilSorumlu();
        return asil != null ? asil.getAd() + " " + asil.getSoyad() : "-";
    }

    /** Stajin TUM gorevleri uzerinden madde ozeti: [tamamlanan, toplam]. */
    private int[] gorevOzeti(Staj staj) {
        int toplam = 0;
        int tamamlanan = 0;
        for (StajGorev gorev : gorevRepository.findByStajIdOrderByAtanmaTarihiAsc(staj.getId())) {
            for (StajGorevMaddesi madde : gorev.getMaddeler()) {
                toplam++;
                if (maddeBitti(madde)) {
                    tamamlanan++;
                }
            }
        }
        return new int[]{tamamlanan, toplam};
    }

    /** StajGorevServiceImpl ile ayni kural: TAMAMLANDI/ONAYLANDI bitti sayilir. */
    private boolean maddeBitti(StajGorevMaddesi madde) {
        return madde.getDurum() == GorevDurumu.TAMAMLANDI || madde.getDurum() == GorevDurumu.ONAYLANDI;
    }

    private String tarih(LocalDate t) {
        return t == null ? "-" : t.format(TARIH);
    }

    private String tarih(LocalDateTime t) {
        return t == null ? "-" : t.format(TARIH);
    }

    private String puanFmt(Double p) {
        if (p == null) {
            return "-";
        }
        // Tam sayiysa "85", degilse "85.5" goster.
        return (p == Math.rint(p)) ? String.valueOf(p.longValue()) : String.valueOf(p);
    }

    private String ad(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String govdeOlustur(Sorumlu sorumlu, List<Staj> stajlar) {
        StringBuilder sb = new StringBuilder();
        sb.append("Merhaba ").append(sorumlu.getAd()).append(" ").append(sorumlu.getSoyad()).append(",\n\n");
        sb.append("Asagidaki stajlarin bitisi yaklasiyor:\n\n");
        for (Staj s : stajlar) {
            long kalan = gunKalan(s);
            sb.append("  - ")
              .append(adSoyad(s))
              .append(" | bitis: ").append(s.getStajBitisTarihi())
              .append(" | ").append(kalan == 0 ? "BUGUN bitiyor" : kalan + " gun kaldi")
              .append(s.getYedekSorumlu() != null && s.getYedekSorumlu().getId().equals(sorumlu.getId())
                      ? " (yedek sorumlusu sizsiniz)" : "")
              .append("\n");
        }
        sb.append("\nDegerlendirme girilmemis stajlar tamamlanmis sayilmaz.\n");
        return sb.toString();
    }

    private long gunKalan(Staj s) {
        return ChronoUnit.DAYS.between(LocalDate.now(), s.getStajBitisTarihi());
    }

    private String adSoyad(Staj s) {
        return s.getStajyer() == null ? "(stajyer yok)"
                : s.getStajyer().getAd() + " " + s.getStajyer().getSoyad();
    }

    private YaklasanBitisResponse toResponse(Staj s) {
        YaklasanBitisResponse r = new YaklasanBitisResponse();
        r.setStajHashId(s.getHashId());
        r.setStajyerAdSoyad(adSoyad(s));
        r.setDepartmanAd(s.getDepartman() != null ? s.getDepartman().getAd() : null);
        r.setStajBitisTarihi(s.getStajBitisTarihi());
        r.setGunKalan(gunKalan(s));
        if (s.getAsilSorumlu() != null) {
            r.setAsilSorumluAdSoyad(s.getAsilSorumlu().getAd() + " " + s.getAsilSorumlu().getSoyad());
        }
        if (s.getYedekSorumlu() != null) {
            r.setYedekSorumluAdSoyad(s.getYedekSorumlu().getAd() + " " + s.getYedekSorumlu().getSoyad());
        }
        return r;
    }
}
