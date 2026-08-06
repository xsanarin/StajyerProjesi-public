package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Departman;
import com.example.StajEnvanterProjesi.entity.Kullanici;
import com.example.StajEnvanterProjesi.entity.Sorumlu;
import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.dto.Sorumlu.SorumluResponse;
import com.example.StajEnvanterProjesi.entity.dto.Sorumlu.SorumluRequest;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DepartmanRepository;
import com.example.StajEnvanterProjesi.repository.KullaniciRepository;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.SorumluService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SorumluServiceImpl implements SorumluService {

    private static final Logger log = LoggerFactory.getLogger(SorumluServiceImpl.class);

    @Autowired
    SorumluRepository sorumluRepository;

    // Audit: kaydi kim (soft) sildi -> giris yapan kullanicidan (SecurityContext) okunur.
    @Autowired
    CurrentUser currentUser;

    // ADIM 1: sorumluyu departmana baglamak icin hashId -> Departman cevrimi.
    @Autowired
    DepartmanRepository departmanRepository;

    // ADIM 3: terfi + hesap kilidi.
    @Autowired
    StajRepository stajRepository;

    @Autowired
    KullaniciRepository kullaniciRepository;

    @Autowired
    MessageHelper messageHelper;

    private SorumluResponse toResponse(Sorumlu sorumlu) { // toResponse Metodu: Veritabanından Gelen Veriyi Maskeleme
        if (sorumlu == null) return null; //Eğer aradığımız sorumlu veritabanında yoksa kod çökmesin diye  boş dönüyoruz

        SorumluResponse response = new SorumluResponse();
        response.setHashId(sorumlu.getHashId());
        response.setAd(sorumlu.getAd());
        response.setSoyad(sorumlu.getSoyad());
        response.setEmail(sorumlu.getEmail());
        response.setTelefon(sorumlu.getTelefon());
        if (sorumlu.getDepartman() != null) {
            response.setDepartmanHashId(sorumlu.getDepartman().getHashId());
            response.setDepartmanAd(sorumlu.getDepartman().getAd());
        }
        return response;
    }

    // Opsiyonel departman: hashId bos ise null, dolu ama bulunamazsa hata.
    private Departman resolveDepartman(String departmanHashId) {
        if (departmanHashId == null || departmanHashId.isBlank()) return null;
        return departmanRepository.findByHashId(departmanHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")));
    }

    @Override
    public SorumluResponse findById(Long id) {
        Optional<Sorumlu> sorumlu = sorumluRepository.findById(id);
        return sorumlu.map(this::toResponse).orElse(null); //eğer veri bulunduysa, yukarıda anlattığım toResponse çevirmenini çağırır ve veriyi DTO'ya çevirir. Bulunamadıysa null döner.
    }

    @Override
    public List<SorumluResponse> findAll() {
        // Ayrilan (status=false) sorumlu listede ve sorumlu secim kutularinda gorunmemeli.
        return sorumluRepository.findByStatusTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Profil guncelleme. @Transactional: e-posta iki tabloda birden tutuluyor
     * (sorumlu.email = goruntulenen bilgi, kullanici.email = LOGIN KIMLIGI). Ikisi ayni
     * islemde guncellenmezse, yanlis yazilmis bir e-posta duzeltildiginde profil dogru
     * gorunur ama kisi hala eski/hatali adresle giris yapmak zorunda kalirdi.
     *
     * Sifreye DOKUNULMAZ: e-posta duzeltmek bir yazim hatasi tamiridir, hesap devri degil.
     */
    @Override
    @Transactional
    public SorumluResponse update(Long id, SorumluRequest request) {
        Optional<Sorumlu> mevcutSorumlu = sorumluRepository.findById(id);
        if (mevcutSorumlu.isPresent()) {
            Sorumlu guncellenecek = mevcutSorumlu.get();

            // Login hesabi (varsa). Ayrilmis sorumlunun hesabi status=false oldugu icin
            // bulunmaz; o durumda yalnizca profil guncellenir, kilitli hesap kilitli kalir.
            Optional<Kullanici> hesap = kullaniciRepository.findBySorumluIdAndStatusTrue(id);

            // Tekillik LOGIN tablosuna karsi kontrol edilir: kullanici.email DB'de UNIQUE
            // (V1__baseline.sql). Profil tablosuna bakmak yetmez, cakisma orada patlardi.
            emailTekilMi(request.getEmail(), hesap.map(Kullanici::getId).orElse(null));

            guncellenecek.setAd(request.getAd());
            guncellenecek.setSoyad(request.getSoyad());
            guncellenecek.setEmail(request.getEmail());
            guncellenecek.setTelefon(request.getTelefon());
            guncellenecek.setDepartman(resolveDepartman(request.getDepartmanHashId()));

            Sorumlu guncellendi = sorumluRepository.save(guncellenecek);

            hesap.ifPresent(k -> {
                if (!k.getEmail().equals(request.getEmail())) {
                    log.info("Sorumlu login e-postasi guncellendi: hedef={} guncelleyen={}",
                            guncellendi.getHashId(), currentUser.aktorEtiketi());
                    k.setEmail(request.getEmail());
                    kullaniciRepository.save(k);
                }
            });

            return toResponse(guncellendi);
        }
        return null;
    }

    /**
     * Yeni e-posta baska bir login hesabinda kullaniliyor mu? {@code haricHesapId}
     * kisinin kendi hesabidir; disarida birakilmazsa kisi e-postasini degistirmeden
     * kaydettiginde kendi kaydiyla cakisirdi.
     */
    private void emailTekilMi(String email, Long haricHesapId) {
        boolean cakisma = haricHesapId == null
                ? kullaniciRepository.existsByEmail(email)
                : kullaniciRepository.existsByEmailAndIdNot(email, haricHesapId);
        if (cakisma) {
            throw new BaseException(messageHelper.get("error.kullanici.mail.mevcut"));
        }
    }

    /**
     * Soft-delete: kayit DURUR (gecmis stajlar ona bagli), yalnizca listelerden kalkar.
     *
     * @Transactional + login hesabinin kilitlenmesi: profil status=false yapilip
     * kullanici.status'e dokunulmadiginda kisi listelerden kayboluyor ama HESABI ACIK
     * kaliyordu -- silinen sorumlu giris yapip gecerli token alabiliyordu. Profil kaydini
     * saklamak ile erisimi surdurmek ayri seyler; sorumluAyril bu ayrimi zaten dogru
     * yapiyordu (4. adim), delete() yolunda eksikti.
     *
     * NOT: Bu uc terfi mantigini CALISTIRMAZ. Aktif stajlari olan bir sorumluyu
     * ayirmak icin sorumluAyril (DELETE /api/admin/sorumlular/{hashId}) kullanilmali.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Optional<Sorumlu> mevcutSorumlu = sorumluRepository.findById(id);
        if (mevcutSorumlu.isPresent()) {
            Sorumlu sorumlu = mevcutSorumlu.get();
            sorumlu.setStatus(false);
            sorumlu.setSilenKullaniciId(currentUser.kullaniciIdOrNull());
            sorumlu.setSilmeTarihi(LocalDateTime.now());
            sorumluRepository.save(sorumlu);
            log.info("Sorumlu soft-delete: hedef={} silen={}", sorumlu.getHashId(), currentUser.aktorEtiketi());

            kullaniciRepository.findBySorumluIdAndStatusTrue(id)
                    .ifPresent(k -> {
                        k.setStatus(false);
                        kullaniciRepository.save(k);
                        log.info("Silinen sorumlunun login hesabi kilitlendi: hedef={}", sorumlu.getHashId());
                    });
        }
    }

    /**
     * ADIM 3 — Terfi transaction'i. @Transactional: adimlarin hepsi ya olur ya hicbiri;
     * DB'de tutarsiz ara durum (asili degismis ama hesabi hala acik gibi) kalmaz.
     */
    @Override
    @Transactional
    public void sorumluAyril(Long sorumluId) {
        Sorumlu sorumlu = sorumluRepository.findById(sorumluId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.sorumlu.bulunamadi", sorumluId)));

        // 1) Asil oldugu DEVAM_EDIYOR stajlarda yedek asile terfi eder.
        List<Staj> asilOlduklari = stajRepository
                .findByAsilSorumluIdAndDurum(sorumluId, StajDurumu.DEVAM_EDIYOR);

        // 1a) ONCE DOGRULA, SONRA DEGISTIR.
        // TEK ASIL KURALI terfi sirasinda da gecerli: yedeklik sinirsiz oldugu icin bir
        // kisi birden fazla stajin yedegi olabilir, ama hepsi birden terfi ederse tek
        // kisi iki stajin asili olur ve kural kirilir. Boyle bir durumda ayrilma islemi
        // reddedilir; admin once o stajlara baska yedek atar. (Yedeksiz staj kontrolu de
        // buraya alindi: mutasyon baslamadan tum engeller gorulsun.)
        Set<Long> terfiEdecekler = new HashSet<>();
        for (Staj s : asilOlduklari) {
            if (s.getYedekSorumlu() == null) {
                throw new BaseException(messageHelper.get("error.sorumlu.yedeksiz.stajyer",
                        s.getStajyer().getAd() + " " + s.getStajyer().getSoyad()));
            }
            Sorumlu yedek = s.getYedekSorumlu();

            // Yedek, baska bir devam eden stajin zaten asili mi?
            boolean baskaStajinAsili = stajRepository
                    .findByAsilSorumluIdAndDurum(yedek.getId(), StajDurumu.DEVAM_EDIYOR)
                    .stream()
                    .anyMatch(x -> Boolean.TRUE.equals(x.getStatus()) && !x.getId().equals(s.getId()));

            // add() false donerse: ayni yedek bu dongude ikinci kez terfi edecekti.
            if (baskaStajinAsili || !terfiEdecekler.add(yedek.getId())) {
                throw new BaseException(messageHelper.get("error.sorumlu.terfi.cakisma",
                        yedek.getAd() + " " + yedek.getSoyad(),
                        s.getStajyer().getAd() + " " + s.getStajyer().getSoyad()));
            }
        }

        // 1b) Dogrulama gecti, terfileri uygula.
        for (Staj s : asilOlduklari) {
            s.setAsilSorumlu(s.getYedekSorumlu());   // TERFI
            s.setYedekSorumlu(null);                 // admin yeni yedek atar
            stajRepository.save(s);
        }

        // 2) Yedek oldugu DEVAM_EDIYOR stajlarda slot bosalir.
        List<Staj> yedekOlduklari = stajRepository
                .findByYedekSorumluIdAndDurum(sorumluId, StajDurumu.DEVAM_EDIYOR);
        for (Staj s : yedekOlduklari) {
            s.setYedekSorumlu(null);
            stajRepository.save(s);
        }

        // 3) Profil soft-delete.
        sorumlu.setStatus(false);
        sorumlu.setSilenKullaniciId(currentUser.kullaniciIdOrNull());
        sorumlu.setSilmeTarihi(LocalDateTime.now());
        sorumluRepository.save(sorumlu);
        log.info("Sorumlu ayrildi (soft-delete + terfi): hedef={} silen={}", sorumlu.getHashId(), currentUser.aktorEtiketi());

        // 4) Login hesabini kilitle -> ayrilan sorumlu artik giris yapamaz (JwtAuthFilter aninda duser).
        kullaniciRepository.findBySorumluIdAndStatusTrue(sorumluId)
                .ifPresent(k -> {
                    k.setStatus(false);
                    kullaniciRepository.save(k);
                });
    }
}
