package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.entity.Kullanici;
import com.example.StajEnvanterProjesi.entity.Stajyer;
import com.example.StajEnvanterProjesi.entity.dto.SayfaResponse;
import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerResponse;
import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerRequest;
import com.example.StajEnvanterProjesi.repository.KullaniciRepository;
import com.example.StajEnvanterProjesi.repository.StajyerRepository;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.StajyerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
public class StajyerServiceImpl implements StajyerService {

    private static final Logger log = LoggerFactory.getLogger(StajyerServiceImpl.class);

    @Autowired
    private StajyerRepository stajyerRepository;

    @Autowired
    private MessageHelper messageHelper;

    // Profil e-postasini login hesabiyla senkron tutmak icin.
    @Autowired
    private KullaniciRepository kullaniciRepository;

    // Audit: kaydi kim ekledi/sildi -> giris yapan kullanicidan (SecurityContext) okunur.
    @Autowired
    private CurrentUser currentUser;

    private StajyerResponse toResponse(Stajyer stajyer) {
        if (stajyer == null) return null;

        StajyerResponse response = new StajyerResponse();
        response.setHashId(stajyer.getHashId());
        response.setAd(stajyer.getAd());
        response.setSoyad(stajyer.getSoyad());
        response.setEmail(stajyer.getEmail());
        response.setTelefon(stajyer.getTelefon());
        response.setUniversite(stajyer.getUniversite());
        response.setBolum(stajyer.getBolum());
        response.setSinif(stajyer.getSinif());
        response.setStatus(stajyer.getStatus());
        return response;
    }

    @Override
    public StajyerResponse findById(Long id) {
        Optional<Stajyer> stajyer = stajyerRepository.findById(id);
        return stajyer.map(this::toResponse).orElse(null);
    }

    /**
     * Ad-soyada gore alfabetik; esitlik ihtimaline karsi id ile kirilir.
     * Sayfalamada siralamanin belirli olmasi sart, yoksa ayni kayit iki
     * sayfada birden cikabilir (bkz. StajServiceImpl).
     */
    private static final Sort VARSAYILAN_SIRA =
            Sort.by(Sort.Order.asc("ad"), Sort.Order.asc("soyad"), Sort.Order.asc("id"));

    @Override
    @Transactional(readOnly = true)
    public SayfaResponse<StajyerResponse> ara(String q, int sayfa, int boyut) {
        return SayfaResponse.of(
                stajyerRepository.ara(SayfaResponse.benzerlik(q),
                        SayfaResponse.istek(sayfa, boyut, VARSAYILAN_SIRA)),
                this::toResponse);
    }

    @Override
    public List<StajyerResponse> findAll() {
        // Silinen (status=false) stajyer listede gorunmemeli.
        return stajyerRepository.findByStatusTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StajyerResponse save(StajyerRequest request) {
        // mail kontrolü (aktif kayitlarda e-posta tekilligi)
        Optional<Stajyer> mevcutAktif = stajyerRepository
                .findByEmailAndStatusTrue(request.getEmail());
        if (mevcutAktif.isPresent()) {
            throw new BaseException(messageHelper.get("error.mail.mevcut"));
        }

        // Mass-assignment korumasi: istemci yalnizca su alanlari set edebilir.
        // id/hashId/status DTO'da yok -> sunucu yonetir (ham entity @RequestBody riski kapandi).
        Stajyer stajyer = new Stajyer();
        stajyer.setAd(request.getAd());
        stajyer.setSoyad(request.getSoyad());
        stajyer.setEmail(request.getEmail());
        stajyer.setTelefon(request.getTelefon());
        stajyer.setUniversite(request.getUniversite());
        stajyer.setBolum(request.getBolum());
        stajyer.setSinif(request.getSinif());
        stajyer.setStatus(true); // ADIM 0: durum/tarih Staj'da; Stajyer sadece kisi.
        stajyer.setOlusturanKullaniciId(currentUser.kullaniciIdOrNull());

        Stajyer kaydedilen = stajyerRepository.save(stajyer);
        log.info("Stajyer olusturuldu: hedef={} olusturan={}", kaydedilen.getHashId(), currentUser.aktorEtiketi());
        return toResponse(kaydedilen);
    }


    /**
     * Profil guncelleme. @Transactional: e-posta iki tabloda birden tutuluyor
     * (stajyer.email = goruntulenen bilgi, kullanici.email = LOGIN KIMLIGI). Ikisi ayni
     * islemde guncellenmezse, yanlis yazilmis bir e-posta duzeltildiginde profil dogru
     * gorunur ama kisi hala eski/hatali adresle giris yapmak zorunda kalirdi.
     *
     * Sifreye DOKUNULMAZ: e-posta duzeltmek bir yazim hatasi tamiridir, hesap devri degil.
     */
    @Override
    @Transactional
    public StajyerResponse update(Long id, StajyerRequest request) {
        Optional<Stajyer> mevcutStajyer = stajyerRepository.findById(id);
        if (mevcutStajyer.isPresent()) {
            Stajyer guncellenecek = mevcutStajyer.get();

            if (!guncellenecek.getEmail().equals(request.getEmail())) {
                // 1) Baska bir aktif stajyer PROFILI ayni e-postayi kullaniyor mu?
                Optional<Stajyer> mailKontrol = stajyerRepository
                        .findByEmailAndStatusTrue(request.getEmail());
                if (mailKontrol.isPresent()) {
                    throw new BaseException(messageHelper.get("error.mail.baska.stajyer"));
                }
            }

            // Login hesabi (varsa). Silinmis stajyerin hesabi bulunmaz; o durumda
            // yalnizca profil guncellenir.
            Optional<Kullanici> hesap = kullaniciRepository.findByStajyerIdAndStatusTrue(id);

            // 2) Tekillik LOGIN tablosuna karsi da kontrol edilmeli: kullanici.email DB'de
            // UNIQUE (V1__baseline.sql) ve stajyer profili baska bir rolun (ornegin sorumlu)
            // login e-postasiyla cakisabilir. Sadece profil tablosuna bakmak bunu kacirirdi.
            Long haricHesapId = hesap.map(Kullanici::getId).orElse(null);
            boolean cakisma = haricHesapId == null
                    ? kullaniciRepository.existsByEmail(request.getEmail())
                    : kullaniciRepository.existsByEmailAndIdNot(request.getEmail(), haricHesapId);
            if (cakisma) {
                throw new BaseException(messageHelper.get("error.kullanici.mail.mevcut"));
            }

            guncellenecek.setAd(request.getAd());
            guncellenecek.setSoyad(request.getSoyad());
            guncellenecek.setEmail(request.getEmail());
            guncellenecek.setTelefon(request.getTelefon());
            guncellenecek.setUniversite(request.getUniversite());
            guncellenecek.setBolum(request.getBolum());
            guncellenecek.setSinif(request.getSinif());

            Stajyer guncellendi = stajyerRepository.save(guncellenecek);

            hesap.ifPresent(k -> {
                if (!k.getEmail().equals(request.getEmail())) {
                    log.info("Stajyer login e-postasi guncellendi: hedef={} guncelleyen={}",
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
     * Soft-delete: kayit DURUR (gecmis staj/devam/puan kayitlari ona bagli), yalnizca
     * listelerden kalkar.
     *
     * @Transactional + login hesabinin kilitlenmesi: profil status=false yapilip
     * kullanici.status'e dokunulmadiginda kisi listelerden kayboluyor ama HESABI ACIK
     * kaliyordu -- silinen stajyer giris yapip gecerli token alabiliyordu. Profil kaydini
     * saklamak ile erisimi surdurmek ayri seyler; sorumluAyril bu ayrimi zaten dogru
     * yapiyordu, delete() yolunda eksikti.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Optional<Stajyer> mevcutStajyer = stajyerRepository.findById(id);
        if (mevcutStajyer.isPresent()) {
            Stajyer stajyer = mevcutStajyer.get();
            stajyer.setStatus(false);
            stajyer.setSilenKullaniciId(currentUser.kullaniciIdOrNull());
            stajyer.setSilmeTarihi(LocalDateTime.now());
            stajyerRepository.save(stajyer);
            log.info("Stajyer soft-delete: hedef={} silen={}", stajyer.getHashId(), currentUser.aktorEtiketi());

            kullaniciRepository.findByStajyerIdAndStatusTrue(id)
                    .ifPresent(k -> {
                        k.setStatus(false);
                        kullaniciRepository.save(k);
                        log.info("Silinen stajyerin login hesabi kilitlendi: hedef={}", stajyer.getHashId());
                    });
        }
    }
}
