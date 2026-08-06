package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Devam;
import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamGuncelleRequest;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamOzetResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.ManuelDevamRequest;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DevamRepository;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.DevamService;
import com.example.StajEnvanterProjesi.service.StajService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DevamServiceImpl implements DevamService {

    private final StajService stajService;        // benimAktifStajim icin
    private final StajRepository stajRepository;  // admin manuel: sahiplik aramadan staj bulmak icin
    private final DevamRepository devamRepository;
    private final MessageHelper messageHelper;
    private final CurrentUser currentUser;   // V15: onayi kimin verdigini yazmak icin

    public DevamServiceImpl(StajService stajService, StajRepository stajRepository,
                            DevamRepository devamRepository, MessageHelper messageHelper,
                            CurrentUser currentUser) {
        this.stajService = stajService;
        this.stajRepository = stajRepository;
        this.devamRepository = devamRepository;
        this.messageHelper = messageHelper;
        this.currentUser = currentUser;
    }

    // ---- Tek tik giris ----

    @Override
    @Transactional
    public void girisYap(Long stajId, Long sorumluId) {
        Staj staj = stajService.benimAktifStajim(stajId, sorumluId);  // sahiplik + aktiflik
        LocalDate bugun = LocalDate.now();

        // Bugunun kaydi varsa onu guncelle, yoksa yeni ac (UNIQUE(staj_id, tarih) sayesinde cakismaz).
        Devam d = devamRepository.findByStajIdAndTarih(stajId, bugun)
                .orElseGet(() -> {
                    Devam yeni = new Devam();
                    yeni.setStaj(staj);
                    yeni.setTarih(bugun);
                    return yeni;
                });

        // Admin gunu soft-delete etmisse ayni satiri yeniden aktiflestir (yeni INSERT unique'i ihlal ederdi).
        if (!Boolean.TRUE.equals(d.getStatus())) {
            d.setStatus(true);
            d.setGirisSaati(null);
            d.setCikisSaati(null);
        }

        if (d.getGirisSaati() != null) {
            throw new BaseException(messageHelper.get("error.devam.giris.tekrar"));
        }

        d.setGirisSaati(LocalDateTime.now());  // saat SUNUCUDAN
        devamRepository.save(d);
    }

    // ---- Tek tik cikis ----

    @Override
    @Transactional
    public void cikisYap(Long stajId, Long sorumluId) {
        stajService.benimAktifStajim(stajId, sorumluId);  // yetki + aktiflik kontrolu
        LocalDate bugun = LocalDate.now();

        Devam d = devamRepository.findByStajIdAndTarihAndStatusTrue(stajId, bugun)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.devam.girissiz.cikis")));

        if (d.getGirisSaati() == null) {
            throw new BaseException(messageHelper.get("error.devam.girissiz.cikis"));
        }
        if (d.getCikisSaati() != null) {
            throw new BaseException(messageHelper.get("error.devam.cikis.tekrar"));
        }

        d.setCikisSaati(LocalDateTime.now());
        devamRepository.save(d);
    }

    // ---- V15: stajyerin kendi beyani (onay bekler) ----

    /**
     * Stajyer kendi girisini bildirir. Sorumlunun girisYap'indan tek farki:
     * kayit onaylandi=false acilir, sorumlu onaylayana kadar devama sayilmaz.
     */
    @Override
    @Transactional
    public void stajyerGirisYap(String stajHashId, Long stajyerId) {
        Staj staj = stajimBildir(stajHashId, stajyerId);
        LocalDate bugun = LocalDate.now();

        Devam d = devamRepository.findByStajIdAndTarih(staj.getId(), bugun)
                .orElseGet(() -> {
                    Devam yeni = new Devam();
                    yeni.setStaj(staj);
                    yeni.setTarih(bugun);
                    return yeni;
                });

        // Silinmis satiri yeniden aktiflestir (yeni INSERT unique kisiti ihlal ederdi).
        if (!Boolean.TRUE.equals(d.getStatus())) {
            d.setStatus(true);
            d.setGirisSaati(null);
            d.setCikisSaati(null);
        }
        // Sorumlu bugunu zaten girmisse (onayli kayit) stajyer uzerine yazamaz.
        if (d.getGirisSaati() != null) {
            throw new BaseException(messageHelper.get("error.devam.giris.tekrar"));
        }

        d.setGirisSaati(LocalDateTime.now());   // saat SUNUCUDAN
        d.setOnaylandi(false);
        d.setOnayTarihi(null);
        d.setOnaylayanKullaniciId(null);
        devamRepository.save(d);
    }

    @Override
    @Transactional
    public void stajyerCikisYap(String stajHashId, Long stajyerId) {
        Staj staj = stajimBildir(stajHashId, stajyerId);
        LocalDate bugun = LocalDate.now();

        Devam d = devamRepository.findByStajIdAndTarihAndStatusTrue(staj.getId(), bugun)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.devam.girissiz.cikis")));

        if (d.getGirisSaati() == null) {
            throw new BaseException(messageHelper.get("error.devam.girissiz.cikis"));
        }
        if (d.getCikisSaati() != null) {
            throw new BaseException(messageHelper.get("error.devam.cikis.tekrar"));
        }

        d.setCikisSaati(LocalDateTime.now());

        // Onay GUNUN TAMAMINI kapsar, yalnizca girisi degil. Sorumlu girisi erken
        // onayladiysa cikis eklenince gun yeniden onaya duser; yoksa cikis saati
        // sorumlunun hic gormedigi bir veri olarak iceri sizardi.
        d.setOnaylandi(false);
        d.setOnayTarihi(null);
        d.setOnaylayanKullaniciId(null);
        devamRepository.save(d);
    }

    /**
     * YAZMA kapisi (stajyer): staj bu stajyerin mi VE hala devam ediyor mu?
     * Okuma kapisindan (stajimOkuStajyer) farki aktiflik sarti — kapanmis staja
     * geriye donuk devam bildirilemez.
     */
    private Staj stajimBildir(String stajHashId, Long stajyerId) {
        Staj staj = stajimOkuStajyer(stajHashId, stajyerId);
        if (staj.getDurum() != StajDurumu.DEVAM_EDIYOR) {
            throw new BaseException(messageHelper.get("error.staj.aktif.degil"));
        }
        return staj;
    }

    // ---- V15: sorumlu onayi ----

    @Override
    @Transactional
    public DevamResponse devamOnayla(Long stajId, String devamHashId, Long sorumluId) {
        // Onay yazma islemi: sahiplik + aktiflik kapisindan gecer.
        stajService.benimAktifStajim(stajId, sorumluId);
        Devam d = devamBul(stajId, devamHashId);

        if (Boolean.TRUE.equals(d.getOnaylandi())) {
            throw new BaseException(messageHelper.get("error.devam.zaten.onayli"));
        }
        d.setOnaylandi(true);
        d.setOnayTarihi(LocalDateTime.now());
        d.setOnaylayanKullaniciId(currentUser.kullaniciIdOrNull());
        return toResponse(devamRepository.save(d));
    }

    /**
     * Sorumlu, stajyerin bildirdigi gunu reddeder (kayit soft-delete edilir).
     * ONAYLANMIS kayit reddedilemez — o artik sorumlunun kendi beyani, duzeltmesi
     * admin isi (yanlislikla onaylanan gun icin admin silme ucu var).
     */
    @Override
    @Transactional
    public void devamReddet(Long stajId, String devamHashId, Long sorumluId) {
        stajService.benimAktifStajim(stajId, sorumluId);
        Devam d = devamBul(stajId, devamHashId);

        if (Boolean.TRUE.equals(d.getOnaylandi())) {
            throw new BaseException(messageHelper.get("error.devam.onayli.reddedilemez"));
        }
        d.setStatus(false);
        devamRepository.save(d);
    }

    // ---- Geriye donuk manuel kayit ----

    @Override
    @Transactional
    public void manuelKayit(Long stajId, Long sorumluId, ManuelDevamRequest req, boolean adminMi) {
        // Sorumlu -> sahiplik + aktiflik; admin -> sahiplik aranmaz, sadece staji bul.
        Staj staj;
        if (adminMi) {
            staj = stajRepository.findById(stajId)
                    .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
            // Sahiplik aranmasa da silinen staja yazilamaz (benimAktifStajim ayni kontrolu
            // sorumlu yolunda yapiyor; admin yolu buradan gectigi icin ayrica gerekiyor).
            if (!Boolean.TRUE.equals(staj.getStatus())) {
                throw new BaseException(messageHelper.get("error.staj.bulunamadi"));
            }
        } else {
            staj = stajService.benimAktifStajim(stajId, sorumluId);
        }

        // Gelecek tarih yasak.
        if (req.getTarih().isAfter(LocalDate.now())) {
            throw new BaseException(messageHelper.get("error.devam.gelecek.tarih"));
        }
        // Ayni gune ikinci AKTIF kayit yasak; soft-delete edilmis satir varsa onu yeniden kullanacagiz.
        Devam mevcut = devamRepository.findByStajIdAndTarih(stajId, req.getTarih()).orElse(null);
        if (mevcut != null && Boolean.TRUE.equals(mevcut.getStatus())) {
            throw new BaseException(messageHelper.get("error.devam.kayit.mevcut"));
        }
        // Cikis varsa giris zorunlu.
        if (req.getCikisSaati() != null && req.getGirisSaati() == null) {
            throw new BaseException(messageHelper.get("error.devam.girissiz.cikis"));
        }

        // Pasif satir varsa yeniden aktiflestir (unique constraint), yoksa yeni ac.
        Devam d = mevcut != null ? mevcut : new Devam();
        d.setStatus(true);
        d.setStaj(staj);
        d.setTarih(req.getTarih());
        d.setGirisSaati(req.getGirisSaati());
        d.setCikisSaati(req.getCikisSaati());
        d.setManuelMi(true);
        // d.setKaydeden(...) -> A'nin Kullanici'si gelince set edilecek (simdilik null).
        devamRepository.save(d);
    }


    /**
     * Aylik takvim — AYIN HER GUNU icin bir satir doner (kaydi olmayan gunler dahil).
     *
     * Onceki surum yalnizca kaydi OLAN gunleri donduruyordu; bu yuzden takvimde bos
     * bir gunun "gelmesi gerekmiyordu" mu yoksa "gelmedi" mi oldugu anlasilmiyordu.
     * Artik stajin haftalik devam plani (Staj.devamGunleri) ile karsilastiriliyor.
     *
     * OKUMA icin benimAktifStajim KULLANILMIYOR: o kapi staj DEVAM_EDIYOR sarti
     * koyuyor, oysa takvime en cok TAMAMLANMIS stajda (degerlendirme aninda)
     * bakiliyor. Burada sahiplik aranir, aktiflik aranmaz.
     */
    @Override
    public List<DevamResponse> aylikDevam(Long stajId, Long sorumluId, int yil, int ay) {
        return takvimUret(stajimOku(stajId, sorumluId), yil, ay);
    }

    /**
     * FRONTEND ADIMI — /me takvimi: stajyerin KENDI devam takvimi.
     * Sahiplik sorumluya degil stajyere gore aranir; aktiflik ARANMAZ (gecmis donem
     * de goruntulenebilir, aynen sorumlu goruntulemesinde oldugu gibi).
     */
    @Override
    public List<DevamResponse> stajyerAylikDevam(String stajHashId, Long stajyerId, int yil, int ay) {
        return takvimUret(stajimOkuStajyer(stajHashId, stajyerId), yil, ay);
    }

    /**
     * Admin takvimi: sahiplik kapisi yok. Kapi yerine dogrudan staj cekilir —
     * /api/admin/** zaten hasRole('ADMIN') ile korunuyor.
     */
    @Override
    public List<DevamResponse> adminAylikDevam(Long stajId, int yil, int ay) {
        return takvimUret(stajBul(stajId), yil, ay);
    }

    @Override
    public DevamOzetResponse adminAylikOzet(Long stajId, int yil, int ay) {
        return ozetUret(stajBul(stajId), yil, ay);
    }

    private Staj stajBul(Long stajId) {
        return stajRepository.findById(stajId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
    }

    /** Ay icindeki her gun icin bir satir uretir (kaydi olmayan gunler dahil). */
    private List<DevamResponse> takvimUret(Staj staj, int yil, int ay) {
        LocalDate ayIlk = LocalDate.of(yil, ay, 1);
        LocalDate aySon = ayIlk.withDayOfMonth(ayIlk.lengthOfMonth());

        // Kayitlari tarihe gore indeksle: her gun icin O(1) bakis.
        Map<LocalDate, Devam> kayitlar = devamRepository
                .findByStajIdAndTarihBetweenAndStatusTrue(staj.getId(), ayIlk, aySon)
                .stream()
                .collect(Collectors.toMap(Devam::getTarih, d -> d));

        List<DevamResponse> takvim = new ArrayList<>();
        for (LocalDate gun = ayIlk; !gun.isAfter(aySon); gun = gun.plusDays(1)) {
            takvim.add(gunSatiri(staj, gun, kayitlar.get(gun)));
        }
        return takvim;
    }

    /**
     * Aylik ozet: kac gun beklenirdi, kac gun gelindi, kac gun devamsiz.
     * Yalnizca GECMIS planli gunler sayilir — bugun ve sonrasi devamsiz sayilmaz.
     */
    @Override
    public DevamOzetResponse aylikOzet(Long stajId, Long sorumluId, int yil, int ay) {
        return ozetUret(stajimOku(stajId, sorumluId), yil, ay);
    }

    /** FRONTEND ADIMI — /me ozeti: stajyerin kendi devam yuzdesi. */
    @Override
    public DevamOzetResponse stajyerAylikOzet(String stajHashId, Long stajyerId, int yil, int ay) {
        return ozetUret(stajimOkuStajyer(stajHashId, stajyerId), yil, ay);
    }

    private DevamOzetResponse ozetUret(Staj staj, int yil, int ay) {
        Long stajId = staj.getId();

        LocalDate ayIlk = LocalDate.of(yil, ay, 1);
        LocalDate aySon = ayIlk.withDayOfMonth(ayIlk.lengthOfMonth());
        LocalDate bugun = LocalDate.now();

        Map<LocalDate, Devam> kayitlar = devamRepository
                .findByStajIdAndTarihBetweenAndStatusTrue(stajId, ayIlk, aySon)
                .stream()
                .collect(Collectors.toMap(Devam::getTarih, d -> d));

        int planli = 0, gelinen = 0, planDisi = 0, onayBekleyen = 0;
        for (LocalDate gun = ayIlk; !gun.isAfter(aySon); gun = gun.plusDays(1)) {
            boolean gunPlanli = planliMi(staj, gun);
            Devam kayit = kayitlar.get(gun);
            // V15: yalnizca ONAYLI kayit "gelindi" sayilir. Onaylanmamis beyan
            // devamsizlikta durur ve ayrica sayilir ki sorumlu bekleyeni gorsun.
            boolean onayli = kayit != null && Boolean.TRUE.equals(kayit.getOnaylandi());

            if (gunPlanli && !gun.isAfter(bugun)) {
                planli++;
                if (onayli) {
                    gelinen++;
                } else if (kayit != null) {
                    onayBekleyen++;
                }
            } else if (!gunPlanli && onayli) {
                planDisi++;   // telafi ya da fazladan gelinen gun
            }
        }

        DevamOzetResponse r = new DevamOzetResponse();
        r.setPlanliGun(planli);
        r.setGelinenGun(gelinen);
        r.setDevamsizGun(planli - gelinen);
        r.setPlanDisiGun(planDisi);
        r.setOnayBekleyenGun(onayBekleyen);
        r.setDevamYuzdesi(planli == 0 ? 0 : (int) Math.round(gelinen * 100.0 / planli));
        return r;
    }

    // ---- Admin: kaydi sil ----

    @Override
    @Transactional
    public void devamSil(Long stajId, String devamHashId) {
        Devam d = devamBul(stajId, devamHashId);
        d.setStatus(false);          // soft delete: satiri silmiyoruz, pasiflestiriyoruz
        devamRepository.save(d);
    }

    // ---- Admin: giris/cikis saatini duzelt ----

    @Override
    @Transactional
    public DevamResponse devamGuncelle(Long stajId, String devamHashId, DevamGuncelleRequest req) {
        Devam d = devamBul(stajId, devamHashId);

        // Cikis varsa giris zorunlu.
        if (req.getCikisSaati() != null && req.getGirisSaati() == null) {
            throw new BaseException(messageHelper.get("error.devam.girissiz.cikis"));
        }
        // Cikis giristen once olamaz.
        if (req.getGirisSaati() != null && req.getCikisSaati() != null
                && req.getCikisSaati().isBefore(req.getGirisSaati())) {
            throw new BaseException(messageHelper.get("error.devam.saat.sirasi"));
        }

        d.setGirisSaati(req.getGirisSaati());
        d.setCikisSaati(req.getCikisSaati());
        // Admin mudahalesi -> kayit artik manuel sayilir.
        d.setManuelMi(true);
        return toResponse(devamRepository.save(d));
    }

    // ---- yardimcilar ----

    // hashId ile kaydi bul + bu kaydin gercekten o staja ait oldugunu dogrula (admin yanlis staj URL'i vermesin).
    private Devam devamBul(Long stajId, String devamHashId) {
        Devam d = devamRepository.findByHashIdAndStatusTrue(devamHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.devam.bulunamadi")));
        if (!d.getStaj().getId().equals(stajId)) {
            throw new BaseException(messageHelper.get("error.devam.bulunamadi"));
        }
        return d;
    }

    /**
     * Tek kayit -> yanit (A'nin admin duzeltme ucu kullanir).
     * planli bayragi kaydin kendi stajinin planindan hesaplanir.
     */
    private DevamResponse toResponse(Devam d) {
        return gunSatiri(d.getStaj(), d.getTarih(), d);
    }

    /** Tek bir takvim gunu: kayit + plan bilgisinden satir uretir. */
    private DevamResponse gunSatiri(Staj staj, LocalDate gun, Devam d) {
        boolean planli = planliMi(staj, gun);

        DevamResponse r = new DevamResponse();
        r.setTarih(gun);
        r.setPlanli(planli);

        if (d != null) {
            r.setHashId(d.getHashId());
            r.setGirisSaati(d.getGirisSaati());
            r.setCikisSaati(d.getCikisSaati());
            r.setManuelMi(d.getManuelMi());
            r.setOnaylandi(d.getOnaylandi());
            // V15: onaylanmamis kayit "geldi" sayilmaz; saatler gorunur ama durum ayri.
            r.setDurum(Boolean.TRUE.equals(d.getOnaylandi()) ? durumHesapla(d) : "ONAY_BEKLIYOR");
            return r;
        }

        // Kayit yok: planli gun gecmiste kaldiysa devamsizlik, degilse beklenen/plansiz.
        r.setManuelMi(false);
        r.setOnaylandi(false);
        if (!planli) {
            r.setDurum("PLANSIZ");
        } else if (gun.isAfter(LocalDate.now())) {
            r.setDurum("BEKLENIYOR");
        } else {
            r.setDurum("GELMEDI");
        }
        return r;
    }

    /**
     * Gun stajin haftalik planinda mi? Staj tarih araligi disindaki gunler
     * plansiz sayilir — staj baslamadan once "gelmedi" denemez.
     */
    private boolean planliMi(Staj staj, LocalDate gun) {
        // Baslangic tarihi YOKSA hicbir gun planli degildir.
        //
        // Eskiden null kontrolu atlanip dogrudan haftanin gunune bakiliyordu:
        // stajin ne zaman basladigi bilinmedigi halde GECMISTEKI her plan gunu
        // "GELMEDI" sayiliyordu. Sonuc: 29 Temmuz'da acilan bir staj Temmuz
        // takviminde 1 Temmuz'dan beri devamsiz gorunuyordu (ve onceki aylarda
        // da devamsizlik uretiyordu). Tarih girilmeden devamsizlik hesaplanamaz.
        if (staj.getStajBaslangicTarihi() == null) {
            return false;
        }
        if (gun.isBefore(staj.getStajBaslangicTarihi())) {
            return false;
        }
        if (staj.getStajBitisTarihi() != null && gun.isAfter(staj.getStajBitisTarihi())) {
            return false;
        }
        Set<DayOfWeek> plan = staj.getDevamGunleri();
        return plan != null && plan.contains(gun.getDayOfWeek());
    }

    /**
     * OKUMA kapisi: sahiplik arar, aktiflik ARAMAZ.
     * Tamamlanmis stajin takvimi de goruntulenebilmeli (degerlendirme ani).
     */
    private Staj stajimOku(Long stajId, Long sorumluId) {
        Staj staj = stajRepository.findById(stajId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
        // Silinen staj yok sayilir (stajimOkuStajyer / benimAktifStajim ile ayni kural):
        // soft-delete yalnizca status'u false yapar, durum DEVAM_EDIYOR kalir.
        if (!Boolean.TRUE.equals(staj.getStatus())) {
            throw new BaseException(messageHelper.get("error.staj.bulunamadi"));
        }
        boolean asil = staj.getAsilSorumlu() != null
                && staj.getAsilSorumlu().getId().equals(sorumluId);
        boolean yedek = staj.getYedekSorumlu() != null
                && staj.getYedekSorumlu().getId().equals(sorumluId);
        if (!asil && !yedek) {
            throw new AccessDeniedException(messageHelper.get("error.staj.yetki.yok"));
        }
        return staj;
    }

    /**
     * OKUMA kapisi (stajyer): staj gercekten bu stajyerin mi?
     * Sorumlu tarafindaki stajimOku ile ayni felsefe — sahiplik aranir, aktiflik aranmaz —
     * ama sahiplik asil/yedek sorumluya degil stajin sahibi stajyere gore kurulur.
     */
    private Staj stajimOkuStajyer(String stajHashId, Long stajyerId) {
        Staj staj = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
        // Silinen staj yok sayilir: soft-delete yalnizca status'u false yapar, durum
        // DEVAM_EDIYOR kalir (benimAktifStajim ile ayni kok neden).
        if (!Boolean.TRUE.equals(staj.getStatus())) {
            throw new BaseException(messageHelper.get("error.staj.bulunamadi"));
        }
        boolean sahibi = staj.getStajyer() != null
                && staj.getStajyer().getId().equals(stajyerId);
        if (!sahibi) {
            throw new AccessDeniedException(messageHelper.get("error.staj.yetki.yok"));
        }
        return staj;
    }

    // TAM = giris+cikis var | CIKIS_YOK = giris var cikis yok | GELMEDI = ikisi de yok
    private String durumHesapla(Devam d) {
        if (d.getGirisSaati() != null && d.getCikisSaati() != null) return "TAM";
        if (d.getGirisSaati() != null) return "CIKIS_YOK";
        return "GELMEDI";
    }
}
