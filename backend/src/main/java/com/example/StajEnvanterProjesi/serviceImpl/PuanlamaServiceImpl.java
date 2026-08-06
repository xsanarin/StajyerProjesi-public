package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Degerlendirme;
import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.DegerlendirmeResponse;
import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.PuanDuzeltRequest;
import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.PuanRequest;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DegerlendirmeRepository;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.service.BildirimService;
import com.example.StajEnvanterProjesi.service.PuanlamaService;
import com.example.StajEnvanterProjesi.service.StajService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ADIM 4 — Puanlama + kapanis.
 * NOT: Sahiplik/aktiflik kontrolu icin plan "B'nin benimAktifStajim'ini cagir" diyor;
 * B henuz teslim etmedigi icin ayni kurali getOwnedActiveStaj ile yerinde uyguladim.
 * B'nin metodu gelince buradaki kontrol onunla degistirilebilir.
 */
@Service
public class PuanlamaServiceImpl implements PuanlamaService {

    private final StajRepository stajRepository;
    private final DegerlendirmeRepository degerlendirmeRepository;
    // Degerlendirme.puanlayanSorumluId duz bir Long kolon (iliski degil); disariya
    // hashId verebilmek icin sorumluyu buradan cozuyoruz.
    private final SorumluRepository sorumluRepository;
    private final MessageHelper messageHelper;
    // Kapanis maili (stajyere puan + yorum + gorev ozeti). Gonderim puanlamayi bloklamaz.
    private final BildirimService bildirimService;
    // Kilit acarken TEK ASIL KURALI dogrulamasi icin (kural StajService'te yasiyor,
    // burada kopyalanmiyor ki iki yer birbirinden ayrisamasin).
    private final StajService stajService;

    public PuanlamaServiceImpl(StajRepository stajRepository,
                               DegerlendirmeRepository degerlendirmeRepository,
                               SorumluRepository sorumluRepository,
                               MessageHelper messageHelper,
                               BildirimService bildirimService,
                               StajService stajService) {
        this.stajRepository = stajRepository;
        this.degerlendirmeRepository = degerlendirmeRepository;
        this.sorumluRepository = sorumluRepository;
        this.messageHelper = messageHelper;
        this.bildirimService = bildirimService;
        this.stajService = stajService;
    }

    @Override
    @Transactional
    public DegerlendirmeResponse puanla(String stajHashId, Long sorumluId, PuanRequest request) {
        // Sadece asil sorumlu + DEVAM_EDIYOR staj.
        Staj staj = getOwnedActiveStaj(stajHashId, sorumluId);

        // 1 staj = 1 puan. Normal akista buraya gelinmez (puanlama staji TAMAMLANDI yapar,
        // ikinci deneme yukaridaki durum kontroluna takilir). Tek giris yolu: admin kilidi
        // acmis, staj tekrar DEVAM_EDIYOR olmus ama degerlendirme kaydi yerinde duruyor.
        if (degerlendirmeRepository.existsByStajId(staj.getId())) {
            throw new BaseException(messageHelper.get("error.staj.zaten.puanlanmis"));
        }

        Degerlendirme d = new Degerlendirme();
        d.setStaj(staj);
        d.setPuan(request.getPuan());
        d.setYorum(request.getYorum());
        d.setPuanlayanSorumluId(sorumluId);
        d = degerlendirmeRepository.save(d);

        // Kapanis: staj kilitlenir (yazma islemleri artik reddedilir).
        staj.setDurum(StajDurumu.TAMAMLANDI);
        stajRepository.save(staj);

        // Stajyere kapanis maili: puan + yorum + gorev ozeti. Mail gonderimi puanlamayi
        // BLOKLAMAZ; adres yoksa/SMTP kapaliysa BildirimService sessizce log'a duser.
        // Transaction hala acik oldugu icin lazy gorev maddeleri sorunsuz yuklenir.
        bildirimService.degerlendirmeTamamlandiBildir(staj, d);

        return toResponse(d, staj);
    }

    @Override
    @Transactional(readOnly = true)
    public DegerlendirmeResponse degerlendirmeGetir(String stajHashId) {
        Staj staj = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
        return degerlendirmeOku(staj);
    }

    @Override
    @Transactional(readOnly = true)
    public DegerlendirmeResponse degerlendirmeGetirSorumlu(String stajHashId, Long sorumluId) {
        return degerlendirmeOku(stajimOku(stajHashId, sorumluId));
    }

    @Override
    @Transactional
    public DegerlendirmeResponse adminPuanDuzelt(String stajHashId, PuanDuzeltRequest request) {
        Staj staj = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
        Degerlendirme d = degerlendirmeRepository.findByStajId(staj.getId())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.puanlanmamis")));

        d.setPuan(request.getPuan());
        d.setYorum(request.getYorum());
        d.setDuzeltmeGerekcesi(request.getGerekce());
        d = degerlendirmeRepository.save(d);

        return toResponse(d, staj);
    }

    @Override
    @Transactional
    public DegerlendirmeResponse adminKilidiAc(String stajHashId) {
        Staj staj = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));

        // TEK ASIL KURALI: kilit acmak staji tekrar DEVAM_EDIYOR yapar, yani asil sorumluyu
        // yeniden mesgul eder. Staj kapaliyken o sorumlu bosa dustugu icin baska bir staja
        // asil atanmis olabilir; kontrol edilmezse kilit acilinca ayni kisi iki devam eden
        // stajin asili olur. Bu durumda DB'deki kismi unique index devreye girer ve kullanici
        // anlamli bir hata yerine bos govdeli 500 alirdi.
        if (staj.getAsilSorumlu() != null) {
            stajService.asilMusaitMiDogrula(staj.getAsilSorumlu().getId(), staj.getId());
        }

        // Uc durum: kapanmis staji tekrar yazilabilir yap.
        staj.setDurum(StajDurumu.DEVAM_EDIYOR);
        stajRepository.save(staj);

        Degerlendirme d = degerlendirmeRepository.findByStajId(staj.getId()).orElse(null);
        return toResponse(d, staj);
    }

    // ---- yardimcilar ----

    /**
     * Puanlanmamis staj bir hata DEGIL, gecerli bir ara durum: toResponse null
     * degerlendirmeyi zaten kaldirip puan alanini bos birakir.
     */
    private DegerlendirmeResponse degerlendirmeOku(Staj staj) {
        Degerlendirme d = degerlendirmeRepository.findByStajId(staj.getId()).orElse(null);
        return toResponse(d, staj);
    }

    /**
     * OKUMA kapisi: sahiplik arar, aktiflik ARAMAZ — DevamServiceImpl.stajimOku ile ayni
     * felsefe. Puanlama yetkisi asil sorumluda (getOwnedActiveStaj) ama okumaya yedek de
     * girer; terfi eden yedek gecmis degerlendirmeyi gorebilmeli.
     */
    private Staj stajimOku(String stajHashId, Long sorumluId) {
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

    /**
     * Sahiplik + aktiflik: yalnizca stajin ASIL sorumlusu, yalnizca DEVAM_EDIYOR stajda islem yapabilir.
     * Yetkisiz erisim -> 403 (AccessDeniedException). Aktif degil -> 400 (BaseException).
     */
    private Staj getOwnedActiveStaj(String stajHashId, Long sorumluId) {
        Staj staj = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));

        boolean asilMi = staj.getAsilSorumlu() != null
                && staj.getAsilSorumlu().getId().equals(sorumluId);
        if (!asilMi) {
            throw new AccessDeniedException(messageHelper.get("error.staj.yetki.yok"));
        }
        if (staj.getDurum() != StajDurumu.DEVAM_EDIYOR) {
            throw new BaseException(messageHelper.get("error.staj.aktif.degil"));
        }
        return staj;
    }

    private DegerlendirmeResponse toResponse(Degerlendirme d, Staj staj) {
        DegerlendirmeResponse r = new DegerlendirmeResponse();
        if (d != null) {
            r.setHashId(d.getHashId());
            r.setPuan(d.getPuan());
            r.setYorum(d.getYorum());
            if (d.getPuanlayanSorumluId() != null) {
                sorumluRepository.findById(d.getPuanlayanSorumluId())
                        .ifPresent(s -> r.setPuanlayanSorumluHashId(s.getHashId()));
            }
            r.setDuzeltmeGerekcesi(d.getDuzeltmeGerekcesi());
        }
        r.setStajHashId(staj.getHashId());
        if (staj.getStajyer() != null) {
            r.setStajyerAdSoyad(staj.getStajyer().getAd() + " " + staj.getStajyer().getSoyad());
        }
        r.setStajDurumu(staj.getDurum());
        return r;
    }
}
