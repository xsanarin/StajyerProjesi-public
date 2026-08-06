package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.GorevSablonu;
import com.example.StajEnvanterProjesi.entity.SablonMaddesi;
import com.example.StajEnvanterProjesi.entity.Sorumlu;
import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.StajGorev;
import com.example.StajEnvanterProjesi.entity.StajGorevMaddesi;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.*;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajResponse;
import com.example.StajEnvanterProjesi.entity.enums.GorevDurumu;
import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.GorevSablonuRepository;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.repository.StajGorevRepository;
import com.example.StajEnvanterProjesi.repository.StajGorevMaddesiRepository;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.service.StajGorevService;
import com.example.StajEnvanterProjesi.service.StajService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StajGorevServiceImpl implements StajGorevService {

    private final StajService stajService;                 // benimAktifStajim (ADIM 1 ortak kapisi)
    private final StajRepository stajRepository;
    private final GorevSablonuRepository sablonRepository;
    private final StajGorevRepository gorevRepository;
    private final StajGorevMaddesiRepository maddeRepository;
    private final SorumluRepository sorumluRepository;
    private final MessageHelper messageHelper;

    public StajGorevServiceImpl(StajService stajService, StajRepository stajRepository,
                                GorevSablonuRepository sablonRepository,
                                StajGorevRepository gorevRepository,
                                StajGorevMaddesiRepository maddeRepository,
                                SorumluRepository sorumluRepository,
                                MessageHelper messageHelper) {
        this.stajService = stajService;
        this.stajRepository = stajRepository;
        this.sablonRepository = sablonRepository;
        this.gorevRepository = gorevRepository;
        this.maddeRepository = maddeRepository;
        this.sorumluRepository = sorumluRepository;
        this.messageHelper = messageHelper;
    }

    // ================= Sorumlu tarafi =================

    @Override
    public List<SablonResponse> onerilenSablonlar(String stajHashId, Long sorumluId) {
        Staj staj = stajOku(stajHashId, sorumluId);
        return sablonRepository
                .findByDepartmanIdAndSeviyeAndAktifTrue(staj.getDepartman().getId(), staj.getSeviye())
                .stream()
                .map(this::sablonToResponse)
                .collect(Collectors.toList());
    }

    /**
     * SNAPSHOT ATAMA — sistemin kalbi.
     *
     * Sablonun maddeleri metin olarak KOPYALANIR. Sablon sonradan degistirilse ya da
     * silinse bile bu kopyalar degismez; kaynakSablon yalnizca "nereden geldi" izidir.
     */
    @Override
    @Transactional
    public StajGorevResponse sablonAta(String stajHashId, String sablonHashId, Long sorumluId) {
        Staj staj = stajYaz(stajHashId, sorumluId);   // sahiplik + aktiflik

        GorevSablonu sablon = sablonRepository.findByHashId(sablonHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.sablon.bulunamadi")));
        if (Boolean.FALSE.equals(sablon.getAktif())) {
            throw new BaseException(messageHelper.get("error.sablon.pasif"));
        }
        if (!sablon.getDepartman().getId().equals(staj.getDepartman().getId())) {
            throw new BaseException(messageHelper.get("error.sablon.departman.uyumsuz"));
        }
        if (sablon.getSeviye() != staj.getSeviye()) {
            throw new BaseException(messageHelper.get("error.sablon.seviye.uyumsuz"));
        }
        // Cift tiklama korumasi: ayni sablon ayni staja iki kez atanmasin.
        if (gorevRepository.existsByStajIdAndKaynakSablonId(staj.getId(), sablon.getId())) {
            throw new BaseException(messageHelper.get("error.sablon.zaten.atanmis"));
        }

        StajGorev gorev = new StajGorev();
        gorev.setStaj(staj);
        gorev.setKaynakSablon(sablon);              // iz — kopya bagimsiz
        gorev.setBaslik(sablon.getAd());            // KOPYA
        gorev.setAciklama(sablon.getAciklama());    // KOPYA
        gorev.setSeviye(sablon.getSeviye());
        gorev.setDurum(GorevDurumu.BEKLIYOR);
        gorev.setAtanmaTarihi(LocalDateTime.now());

        for (SablonMaddesi madde : sablon.getMaddeler()) {
            StajGorevMaddesi kopya = new StajGorevMaddesi();
            kopya.setSira(madde.getSira());
            kopya.setMaddeMetni(madde.getMaddeMetni());   // METIN kopyalanir, referans DEGIL
            kopya.setOzelMi(false);                       // sablondan geldi
            kopya.setDurum(GorevDurumu.BEKLIYOR);
            gorev.maddeEkle(kopya);
        }

        return toResponse(gorevRepository.save(gorev));
    }

    @Override
    public List<StajGorevResponse> stajGorevleri(String stajHashId, Long sorumluId) {
        Staj staj = stajOku(stajHashId, sorumluId);
        return gorevRepository.findByStajIdOrderByAtanmaTarihiAsc(staj.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StajGorevResponse ozelMaddeEkle(String gorevHashId, OzelMaddeRequest request, Long sorumluId) {
        StajGorev gorev = gorevBul(gorevHashId);
        // Yazma kapisi: staj benim mi + DEVAM_EDIYOR mu. Kapanmis staja madde eklenemez.
        stajYaz(gorev.getStaj().getHashId(), sorumluId);

        int sonrakiSira = maddeRepository.findFirstByStajGorevIdOrderBySiraDesc(gorev.getId())
                .map(s -> s.getSira() + 1)
                .orElse(1);

        StajGorevMaddesi madde = new StajGorevMaddesi();
        madde.setMaddeMetni(request.getMaddeMetni());
        madde.setSira(sonrakiSira);
        madde.setOzelMi(true);                       // sablonda yok, sonradan eklendi
        madde.setDurum(GorevDurumu.BEKLIYOR);
        gorev.maddeEkle(madde);

        // Yeni madde geldi -> gorev tekrar acilir (TAMAMLANDI ise BEKLIYOR'a duser).
        // Aksi halde 8/8 tamamlanmis bir goreve madde eklenince gorev "bitti" gorunur
        // ama icinde yapilmamis is kalirdi.
        durumTazele(gorev);
        return toResponse(gorevRepository.save(gorev));
    }

    @Override
    @Transactional
    public StajGorevResponse ozelMaddeSil(String maddeHashId, Long sorumluId) {
        StajGorevMaddesi madde = maddeBul(maddeHashId);
        StajGorev gorev = madde.getStajGorev();
        stajYaz(gorev.getStaj().getHashId(), sorumluId);

        // Sablondan gelen madde SNAPSHOT'in parcasi: cikarilirsa gorev artik
        // sablonun dondurulmus kopyasi olmaktan cikar. Yalnizca sonradan
        // eklenen ozel madde silinebilir.
        if (!Boolean.TRUE.equals(madde.getOzelMi())) {
            throw new BaseException(messageHelper.get("error.gorev.madde.sablondan"));
        }
        // Onaylanmis madde is kaydidir (onaylayan + onay tarihi yazili); silinirse
        // stajyerin yaptigi is ve sorumlunun onayi geriye donuk kaybolurdu.
        if (madde.getDurum() == GorevDurumu.ONAYLANDI) {
            throw new BaseException(messageHelper.get("error.gorev.madde.onayli.silinemez"));
        }

        // orphanRemoval=true: koleksiyondan cikarmak satiri siler.
        // Karsilastirma id uzerinden — Lombok @Data'nin equals'ina guvenilmez.
        gorev.getMaddeler().removeIf(m -> m.getId().equals(madde.getId()));

        // Engel kalkti: kalan maddelerin hepsi bittiyse gorev tekrar TAMAMLANDI olur.
        durumTazele(gorev);
        return toResponse(gorevRepository.save(gorev));
    }

    @Override
    public IlerlemeResponse ilerleme(String gorevHashId, Long sorumluId) {
        StajGorev gorev = gorevBul(gorevHashId);
        stajOku(gorev.getStaj().getHashId(), sorumluId);
        return ilerlemeHesapla(gorev);
    }

    @Override
    @Transactional
    public StajGorevMaddeResponse maddeOnayla(String maddeHashId, MaddeOnayRequest request, Long sorumluId) {
        if (request.getDurum() != GorevDurumu.ONAYLANDI && request.getDurum() != GorevDurumu.REDDEDILDI) {
            throw new BaseException(messageHelper.get("error.gorev.onay.durum"));
        }
        // Ret gerekce ISTER: stajyer neyi duzeltecegini bilmeli. Onayda not opsiyonel.
        if (request.getDurum() == GorevDurumu.REDDEDILDI
                && (request.getOnayNotu() == null || request.getOnayNotu().isBlank())) {
            throw new BaseException(messageHelper.get("error.gorev.ret.notsuz"));
        }

        StajGorevMaddesi madde = maddeBul(maddeHashId);
        StajGorev gorev = madde.getStajGorev();
        stajYaz(gorev.getStaj().getHashId(), sorumluId);

        // Yalnizca stajyerin bitirdigi madde onaylanir/reddedilir.
        if (madde.getDurum() != GorevDurumu.TAMAMLANDI) {
            throw new BaseException(messageHelper.get("error.gorev.madde.tamamlanmamis"));
        }

        Sorumlu onaylayan = sorumluRepository.findById(sorumluId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.sorumlu.bulunamadi")));

        madde.setDurum(request.getDurum());
        madde.setOnaylayan(onaylayan);
        madde.setOnayTarihi(LocalDateTime.now());
        madde.setOnayNotu(request.getOnayNotu());
        maddeRepository.save(madde);

        // REDDEDILDI gorevi tekrar BEKLIYOR'a dusurur (stajyer yeniden yapacak).
        durumTazele(gorev);
        gorevRepository.save(gorev);

        return maddeToResponse(madde);
    }

    @Override
    @Transactional
    public StajResponse seviyeYukselt(String stajHashId, Seviye yeniSeviye, Long sorumluId) {
        Staj staj = stajYaz(stajHashId, sorumluId);
        Seviye mevcut = staj.getSeviye();

        // Yalnizca ILERI: enum sirasi LEVEL_1 -> LEVEL_2.
        if (yeniSeviye.ordinal() <= mevcut.ordinal()) {
            throw new BaseException(messageHelper.get("error.seviye.geri.alinamaz"));
        }
        // Mevcut seviyede hic gorev atanmamissa yukseltme anlamsiz.
        if (!gorevRepository.existsByStajIdAndSeviye(staj.getId(), mevcut)) {
            throw new BaseException(messageHelper.get("error.seviye.gorev.yok"));
        }
        // Mevcut seviyenin TUM gorevleri TAMAMLANDI olmali.
        if (gorevRepository.existsByStajIdAndSeviyeAndDurumNot(staj.getId(), mevcut, GorevDurumu.TAMAMLANDI)) {
            throw new BaseException(messageHelper.get("error.seviye.gorev.bitmedi"));
        }

        staj.setSeviye(yeniSeviye);
        stajRepository.save(staj);
        return stajService.findById(staj.getId());
    }

    // ================= Stajyer tarafi =================

    @Override
    public List<StajResponse> stajlarim(Long stajyerId) {
        return stajService.stajyerStajlarim(stajyerId);
    }

    @Override
    public List<StajGorevResponse> gorevlerim(String stajHashId, Long stajyerId) {
        Staj staj = stajyerStaji(stajHashId, stajyerId);
        return gorevRepository.findByStajIdOrderByAtanmaTarihiAsc(staj.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StajGorevMaddeResponse maddeTamamla(String maddeHashId, MaddeTamamlaRequest request, Long stajyerId) {
        StajGorevMaddesi madde = maddeBul(maddeHashId);
        StajGorev gorev = madde.getStajGorev();

        // Kendi stajina ait mi + staj aktif mi (tamamlanan staj SALT-OKUNUR).
        Staj staj = stajyerStaji(gorev.getStaj().getHashId(), stajyerId);
        if (staj.getDurum() != StajDurumu.DEVAM_EDIYOR) {
            throw new BaseException(messageHelper.get("error.staj.aktif.degil"));
        }
        // Onaylanmis madde kilitli; reddedilen yeniden isaretlenebilir.
        if (madde.getDurum() == GorevDurumu.ONAYLANDI) {
            throw new BaseException(messageHelper.get("error.gorev.madde.onayli"));
        }

        // Not OPSIYONEL: madde bir soru degil, yapilacak is. Stajyer hicbir sey
        // yazmadan da "bunu yaptim" diyebilmeli. Not gonderilmediyse (null) mevcut
        // not korunur; bosaltmak isteyen bos string gonderir.
        if (request.getStajyerNotu() != null) {
            madde.setStajyerNotu(request.getStajyerNotu());
        }

        // Onay alanlari (onaylayan/onayTarihi/onayNotu) BILEREK TEMIZLENMIYOR.
        // Reddedilen madde yeniden tamamlandiginda ret gerekcesi ekranda kalmali:
        // stajyer neyi duzelttigini gosterebilsin, sorumlu ne istedigini hatirlasin.
        // Durum degistigi icin bu alanlar artik "onceki geri bildirim" anlamina gelir.
        madde.setDurum(Boolean.TRUE.equals(request.getTamamlandi())
                ? GorevDurumu.TAMAMLANDI
                : GorevDurumu.BEKLIYOR);
        maddeRepository.save(madde);

        durumTazele(gorev);
        gorevRepository.save(gorev);

        return maddeToResponse(madde);
    }

    // ================= Ortak yardimcilar =================

    /**
     * YAZMA kapisi — ADIM 1'in ortak sozlesmesi: sahiplik + staj DEVAM_EDIYOR.
     */
    private Staj stajYaz(String stajHashId, Long sorumluId) {
        Staj staj = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
        return stajService.benimAktifStajim(staj.getId(), sorumluId);
    }

    /**
     * OKUMA kapisi — sahiplik arar ama aktiflik ARAMAZ.
     * benimAktifStajim burada kullanilamaz: tamamlanmis stajin gorevleri de
     * goruntulenebilmeli (gecmis salt-okunur gorunur, KISI-B.md ADIM 2).
     */
    private Staj stajOku(String stajHashId, Long sorumluId) {
        Staj staj = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));

        boolean asil = staj.getAsilSorumlu() != null
                && staj.getAsilSorumlu().getId().equals(sorumluId);
        boolean yedek = staj.getYedekSorumlu() != null
                && staj.getYedekSorumlu().getId().equals(sorumluId);
        if (!asil && !yedek) {
            throw new AccessDeniedException(messageHelper.get("error.staj.yetki.yok"));
        }
        return staj;
    }

    /** Stajyer sahipligi: staj gercekten bu stajyerin mi? */
    private Staj stajyerStaji(String stajHashId, Long stajyerId) {
        Staj staj = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
        if (staj.getStajyer() == null || !staj.getStajyer().getId().equals(stajyerId)) {
            throw new AccessDeniedException(messageHelper.get("error.staj.yetki.yok"));
        }
        return staj;
    }

    private StajGorev gorevBul(String hashId) {
        return gorevRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.gorev.bulunamadi")));
    }

    private StajGorevMaddesi maddeBul(String hashId) {
        return maddeRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.gorev.madde.bulunamadi")));
    }

    /**
     * Gorev durumu maddelerdan TUREYIR, elle set edilmez:
     * tum maddeler bitmisse TAMAMLANDI, aksi halde BEKLIYOR.
     * Maddesuz gorev (tum maddeler silinmis) TAMAMLANDI sayilmaz.
     */
    private void durumTazele(StajGorev gorev) {
        boolean hepsiBitti = !gorev.getMaddeler().isEmpty()
                && gorev.getMaddeler().stream().allMatch(this::maddeBitti);
        gorev.setDurum(hepsiBitti ? GorevDurumu.TAMAMLANDI : GorevDurumu.BEKLIYOR);
    }

    /** TAMAMLANDI ve ONAYLANDI "bitti" sayilir; REDDEDILDI tekrar yapilmali. */
    private boolean maddeBitti(StajGorevMaddesi madde) {
        return madde.getDurum() == GorevDurumu.TAMAMLANDI || madde.getDurum() == GorevDurumu.ONAYLANDI;
    }

    private IlerlemeResponse ilerlemeHesapla(StajGorev gorev) {
        int toplam = gorev.getMaddeler().size();
        int tamamlanan = (int) gorev.getMaddeler().stream().filter(this::maddeBitti).count();

        IlerlemeResponse r = new IlerlemeResponse();
        r.setToplam(toplam);
        r.setTamamlanan(tamamlanan);
        r.setYuzde(toplam == 0 ? 0 : (int) Math.round(tamamlanan * 100.0 / toplam));
        return r;
    }

    // ---- ceviriciler ----

    private StajGorevResponse toResponse(StajGorev g) {
        StajGorevResponse r = new StajGorevResponse();
        r.setHashId(g.getHashId());
        r.setBaslik(g.getBaslik());
        r.setAciklama(g.getAciklama());
        r.setSeviye(g.getSeviye());
        r.setDurum(g.getDurum());
        r.setAtanmaTarihi(g.getAtanmaTarihi());
        // Sablon silinmisse null (ON DELETE SET NULL) — gorev yine de eksiksiz doner.
        r.setKaynakSablonHashId(g.getKaynakSablon() != null ? g.getKaynakSablon().getHashId() : null);
        r.setIlerleme(ilerlemeHesapla(g));
        r.setMaddeler(g.getMaddeler().stream().map(this::maddeToResponse).collect(Collectors.toList()));
        return r;
    }

    private StajGorevMaddeResponse maddeToResponse(StajGorevMaddesi s) {
        StajGorevMaddeResponse r = new StajGorevMaddeResponse();
        r.setHashId(s.getHashId());
        r.setMaddeMetni(s.getMaddeMetni());
        r.setSira(s.getSira());
        r.setOzelMi(s.getOzelMi());
        r.setStajyerNotu(s.getStajyerNotu());
        r.setDurum(s.getDurum());
        r.setOnayTarihi(s.getOnayTarihi());
        r.setOnayNotu(s.getOnayNotu());
        return r;
    }

    private SablonResponse sablonToResponse(GorevSablonu s) {
        SablonResponse r = new SablonResponse();
        r.setHashId(s.getHashId());
        r.setAd(s.getAd());
        r.setAciklama(s.getAciklama());
        r.setSeviye(s.getSeviye());
        r.setAktif(s.getAktif());
        if (s.getDepartman() != null) {
            r.setDepartmanHashId(s.getDepartman().getHashId());
            r.setDepartmanAd(s.getDepartman().getAd());
        }
        r.setMaddeler(s.getMaddeler().stream().map(madde -> {
            SablonMaddeResponse sr = new SablonMaddeResponse();
            sr.setHashId(madde.getHashId());
            sr.setMaddeMetni(madde.getMaddeMetni());
            sr.setSira(madde.getSira());
            return sr;
        }).collect(Collectors.toList()));
        return r;
    }
}
