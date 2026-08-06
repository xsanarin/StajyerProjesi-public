package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Departman;
import com.example.StajEnvanterProjesi.entity.Donem;
import com.example.StajEnvanterProjesi.entity.Sorumlu;
import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.Stajyer;
import com.example.StajEnvanterProjesi.entity.dto.SayfaResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.DepartmanIstatistikResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajGuncelleRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajResponse;
import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DepartmanRepository;
import com.example.StajEnvanterProjesi.repository.DonemRepository;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.repository.StajyerRepository;
import com.example.StajEnvanterProjesi.entity.dto.Staj.DepartmanDegistirRequest;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import com.example.StajEnvanterProjesi.service.StajService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StajServiceImpl implements StajService {

    // Staj bir kavsak tablo -> iliskili varliklari cekmek icin diger repository'ler de lazim.
    @Autowired StajRepository stajRepository;
    @Autowired StajyerRepository stajyerRepository;
    @Autowired DonemRepository donemRepository;
    @Autowired SorumluRepository sorumluRepository;
    @Autowired DepartmanRepository departmanRepository;   // ADIM 1: hashId -> Departman
    @Autowired MessageHelper messageHelper;

    // ---- Cevirici ----

    private StajResponse toResponse(Staj s) {
        if (s == null) return null;
        StajResponse r = new StajResponse();
        r.setHashId(s.getHashId());

        if (s.getStajyer() != null) {
            r.setStajyerHashId(s.getStajyer().getHashId());
            r.setStajyerAdSoyad(s.getStajyer().getAd() + " " + s.getStajyer().getSoyad());
        }
        if (s.getDonem() != null) {
            r.setDonemHashId(s.getDonem().getHashId());
            r.setDonemAd(s.getDonem().getAd());
        }
        if (s.getDepartman() != null) {
            r.setDepartmanHashId(s.getDepartman().getHashId());
            r.setDepartmanAd(s.getDepartman().getAd());
        }
        if (s.getAsilSorumlu() != null) {
            r.setAsilSorumluHashId(s.getAsilSorumlu().getHashId());
            r.setAsilSorumluAdSoyad(s.getAsilSorumlu().getAd() + " " + s.getAsilSorumlu().getSoyad());
        }
        if (s.getYedekSorumlu() != null) {
            r.setYedekSorumluHashId(s.getYedekSorumlu().getHashId());
            r.setYedekSorumluAdSoyad(s.getYedekSorumlu().getAd() + " " + s.getYedekSorumlu().getSoyad());
        }
        r.setDurum(s.getDurum());
        r.setSeviye(s.getSeviye());
        r.setStajBaslangicTarihi(s.getStajBaslangicTarihi());
        r.setStajBitisTarihi(s.getStajBitisTarihi());
        r.setDevamGunleri(s.getDevamGunleri());
        return r;
    }

    // ---- CRUD ----

    @Override
    public StajResponse findById(Long id) {
        return stajRepository.findById(id).map(this::toResponse).orElse(null);
    }

    @Override
    public List<StajResponse> findAll() {
        // Silinen (status=false) staj listede gorunmemeli.
        return stajRepository.findByStatusTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Siralama sabit: en yeni staj ustte.
     *
     * Sayfalamanin dogru calismasi icin siralamanin BELIRLI olmasi sart —
     * siralamasiz sorguda Postgres satir sirasini garanti etmez, ayni kayit
     * iki sayfada birden cikabilir ya da hic cikmayabilir.
     */
    private static final Sort VARSAYILAN_SIRA = Sort.by(Sort.Direction.DESC, "id");

    @Override
    @Transactional(readOnly = true)
    public SayfaResponse<StajResponse> ara(String q, String departmanHashId, StajDurumu durum,
                                           int sayfa, int boyut) {
        Long departmanId = null;
        if (departmanHashId != null && !departmanHashId.isBlank()) {
            departmanId = departmanRepository.findByHashId(departmanHashId)
                    .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")))
                    .getId();
        }

        return SayfaResponse.of(
                stajRepository.ara(SayfaResponse.benzerlik(q), departmanId, durum,
                        SayfaResponse.istek(sayfa, boyut, VARSAYILAN_SIRA)),
                this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmanIstatistikResponse> departmanIstatistigi(boolean sadeceAktif) {
        // null = durum filtresi yok (tum stajlar, kapanmislar dahil).
        return stajRepository.departmanaGoreStajyerSayisi(
                sadeceAktif ? StajDurumu.DEVAM_EDIYOR : null);
    }

    @Override
    public StajResponse save(StajRequest request) {
        // 1) IS KURALLARI (plan). Iskelet: dogrulamalari acikca gosteriyoruz.

        // asil ZORUNLU, yedek OPSIYONEL: bir departmanda tek sorumlu calisiyor olabilir,
        // o durumda yedek atanacak kimse yok. Bos string de "yok" demektir (form bos
        // secim gonderdiginde "" gelir, null gelmez).
        if (request.getAsilSorumluHashId() == null || request.getAsilSorumluHashId().isBlank()) {
            throw new BaseException(messageHelper.get("error.staj.sorumlu.zorunlu"));
        }

        // 2) Iliskili varliklari getir (DTO kurali: disaridan hashId gelir, yoksa hata).
        // Kimlik karsilastirmalari asagida entity id'si uzerinden yapilir; bu yuzden
        // varlik cozumlemesi is kurallarindan once gelir.
        Stajyer stajyer = stajyerRepository.findByHashId(request.getStajyerHashId())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.stajyer.bulunamadi")));
        Donem donem = donemRepository.findByHashId(request.getDonemHashId())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.donem.bulunamadi")));
        Sorumlu asil = sorumluRepository.findByHashId(request.getAsilSorumluHashId())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.asil.sorumlu.bulunamadi")));
        // Yedek verilmediyse null kalir; verildiyse gercekten var olmali.
        Sorumlu yedek = yedekCoz(request.getYedekSorumluHashId());

        // ayni stajda asil = yedek olamaz (CHECK kuralinin servis tarafi)
        if (yedek != null && asil.getId().equals(yedek.getId())) {
            throw new BaseException(messageHelper.get("error.staj.sorumlu.ayni"));
        }
        // ayni kisi ayni donemde iki kez kaydedilemez (UNIQUE kuralinin servis tarafi)
        if (stajRepository.existsByStajyerIdAndDonemId(stajyer.getId(), donem.getId())) {
            throw new BaseException(messageHelper.get("error.staj.donemde.kayitli"));
        }

        // 2b) ADIM 1 — DEPARTMAN ESLESMESI:
        // Departman staja baglidir; asil ve yedek sorumlunun departmani stajin departmani
        // ile ayni olmali (Java stajina .NET'ci sorumlu atanamaz).
        Departman departman = departmanRepository.findByHashId(request.getDepartmanHashId())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")));

        if (!departmanEslesiyor(asil, departman)
                || (yedek != null && !departmanEslesiyor(yedek, departman))) {
            throw new BaseException(messageHelper.get("error.departman.eslesmiyor"));
        }

        // 2c) TEK ASIL KURALI: yeni staj oldugu icin haric tutulacak kayit yok.
        asilMusaitMiDogrula(asil.getId(), null);

        // 3) Entity'yi kur ve kaydet
        Staj s = new Staj();
        s.setStajyer(stajyer);
        s.setDonem(donem);
        s.setDepartman(departman);
        s.setAsilSorumlu(asil);
        s.setYedekSorumlu(yedek);
        // ADIM 0: seviye/durum artik enum. null gelirse entity varsayilani (LEVEL_1 / DEVAM_EDIYOR) gecerli.
        if (request.getSeviye() != null) {
            s.setSeviye(request.getSeviye());
        }

        // ADIM 3 (KISI-B): staj tarihleri. Verilmezse donemin tarihleri devralinir —
        // cogu staj donemle ayni tarihte baslayip biter, her seferinde yazdirmak gereksiz.
        s.setStajBaslangicTarihi(request.getStajBaslangicTarihi() != null
                ? request.getStajBaslangicTarihi() : donem.getBaslangicTarihi());
        s.setStajBitisTarihi(request.getStajBitisTarihi() != null
                ? request.getStajBitisTarihi() : donem.getBitisTarihi());

        // V13: haftalik devam plani. Verilmezse entity varsayilani (Pzt-Cum) gecerli.
        if (request.getDevamGunleri() != null && !request.getDevamGunleri().isEmpty()) {
            s.setDevamGunleri(new LinkedHashSet<>(request.getDevamGunleri()));
        }

        if (s.getStajBaslangicTarihi() != null && s.getStajBitisTarihi() != null
                && s.getStajBitisTarihi().isBefore(s.getStajBaslangicTarihi())) {
            throw new BaseException(messageHelper.get("error.staj.tarih.sirasi"));
        }

        return toResponse(stajRepository.save(s));
    }

    /**
     * Yedek sorumlu OPSIYONEL: bir departmanda tek sorumlu calisiyor olabilir ve o zaman
     * atanacak yedek yoktur. Verilmemis (null/bos) ise null doner — staj yedeksiz acilir.
     * Verilmisse gercekten var olmali; yanlis hashId sessizce "yedek yok"a donusmemeli.
     */
    private Sorumlu yedekCoz(String yedekSorumluHashId) {
        if (yedekSorumluHashId == null || yedekSorumluHashId.isBlank()) {
            return null;
        }
        return sorumluRepository.findByHashId(yedekSorumluHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.yedek.sorumlu.bulunamadi")));
    }

    // Sorumlunun departmani, stajin departmani ile ayni mi? (departmansiz sorumlu eslesmez)
    private boolean departmanEslesiyor(Sorumlu sorumlu, Departman departman) {
        return sorumlu.getDepartman() != null
                && sorumlu.getDepartman().getId().equals(departman.getId());
    }

    /**
     * TEK ASIL KURALI — bir sorumlu ayni anda yalnizca BIR devam eden stajin asili olabilir.
     *
     * Yedeklik kisitlanmaz: ayni kisi istedigi kadar stajin yedegi olabilir. Kural sadece
     * DEVAM_EDIYOR stajlari sayar, yani staj TAMAMLANDI olunca sorumlu kendiliginden bosa
     * duser; ayrica bir "serbest birak" islemi gerekmez.
     *
     * Soft-delete edilen staj sayilmaz: delete() yalnizca status=false yapar, durum
     * DEVAM_EDIYOR olarak kalir. Status suzgeci olmadan silinmis bir staj sorumluyu
     * sonsuza kadar mesgul gosterirdi.
     *
     * haricStajId = guncellenen stajin kendi id'si (yeni stajda null). Disarida
     * birakilmazsa staj kendi kaydiyla cakisip her guncellemede hata verirdi.
     */
    @Override
    public void asilMusaitMiDogrula(Long asilSorumluId, Long haricStajId) {
        stajRepository.findByAsilSorumluIdAndDurum(asilSorumluId, StajDurumu.DEVAM_EDIYOR)
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getStatus()))
                .filter(s -> haricStajId == null || !s.getId().equals(haricStajId))
                .findFirst()
                .ifPresent(cakisan -> {
                    throw new BaseException(messageHelper.get("error.staj.asil.mesgul",
                            cakisan.getStajyer().getAd() + " " + cakisan.getStajyer().getSoyad()));
                });
    }

    // ---- ADIM 3: bosalan yedek slotunu doldur ----

    @Override
    @Transactional
    public StajResponse yedekSorumluAta(String stajHashId, String yedekSorumluHashId) {
        Staj s = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));
        Sorumlu yedek = sorumluRepository.findByHashId(yedekSorumluHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.yedek.sorumlu.bulunamadi")));

        // asil = yedek olamaz (CHECK kuralinin servis tarafi)
        if (s.getAsilSorumlu() != null && s.getAsilSorumlu().getId().equals(yedek.getId())) {
            throw new BaseException(messageHelper.get("error.staj.sorumlu.ayni"));
        }
        // ADIM 1 departman eslesmesi burada da gecerli.
        if (!departmanEslesiyor(yedek, s.getDepartman())) {
            throw new BaseException(messageHelper.get("error.departman.eslesmiyor"));
        }

        s.setYedekSorumlu(yedek);
        return toResponse(stajRepository.save(s));
    }

    // ---- Staj duzeltme (tarih / devam plani / asil sorumlu) ----

    @Override
    @Transactional
    public StajResponse guncelle(String stajHashId, StajGuncelleRequest request) {
        Staj s = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));

        // Kapanmis staj gecmis veridir; departmanDegistir ile ayni kural.
        if (s.getDurum() != StajDurumu.DEVAM_EDIYOR) {
            throw new BaseException(messageHelper.get("error.staj.aktif.degil"));
        }

        if (request.getAsilSorumluHashId() != null) {
            Sorumlu asil = sorumluRepository.findByHashId(request.getAsilSorumluHashId())
                    .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.asil.sorumlu.bulunamadi")));
            if (s.getYedekSorumlu() != null && s.getYedekSorumlu().getId().equals(asil.getId())) {
                throw new BaseException(messageHelper.get("error.staj.asil.yedek.ayni"));
            }
            // ADIM 1 eslesme kurali: yeni asil, stajin departmanina ait olmali.
            if (!departmanEslesiyor(asil, s.getDepartman())) {
                throw new BaseException(messageHelper.get("error.departman.eslesmiyor"));
            }
            // TEK ASIL KURALI: bu stajin kendisi haric tutulur (asil degismemis olabilir).
            asilMusaitMiDogrula(asil.getId(), s.getId());
            s.setAsilSorumlu(asil);
        }

        if (request.getDevamGunleri() != null) {
            if (request.getDevamGunleri().isEmpty()) {
                throw new BaseException(messageHelper.get("error.staj.devamgunu.bos"));
            }
            s.setDevamGunleri(new LinkedHashSet<>(request.getDevamGunleri()));
        }

        // Kismi guncelleme: gonderilmeyen tarih oldugu gibi kalir.
        if (request.getStajBaslangicTarihi() != null) {
            s.setStajBaslangicTarihi(request.getStajBaslangicTarihi());
        }
        if (request.getStajBitisTarihi() != null) {
            s.setStajBitisTarihi(request.getStajBitisTarihi());
        }

        // Sira kontrolu tek tarih degisse bile calismali: karsilastirma entity uzerinden.
        if (s.getStajBaslangicTarihi() != null && s.getStajBitisTarihi() != null
                && s.getStajBitisTarihi().isBefore(s.getStajBaslangicTarihi())) {
            throw new BaseException(messageHelper.get("error.staj.tarih.sirasi"));
        }

        return toResponse(stajRepository.save(s));
    }

    // ---- Departman duzeltme ----

    @Override
    @Transactional
    public StajResponse departmanDegistir(String stajHashId, DepartmanDegistirRequest request) {
        Staj s = stajRepository.findByHashIdAndStatusTrue(stajHashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));

        // Kapanmis staj gecmis veridir; puani ve departmani artik degismez.
        if (s.getDurum() != StajDurumu.DEVAM_EDIYOR) {
            throw new BaseException(messageHelper.get("error.staj.aktif.degil"));
        }

        Departman departman = departmanRepository.findByHashId(request.getDepartmanHashId())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")));
        Sorumlu asil = sorumluRepository.findByHashId(request.getAsilSorumluHashId())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.asil.sorumlu.bulunamadi")));
        // Yedek burada da opsiyonel: yeni departmanda tek sorumlu varsa staj yedeksiz kalir.
        Sorumlu yedek = yedekCoz(request.getYedekSorumluHashId());

        if (yedek != null && asil.getId().equals(yedek.getId())) {
            throw new BaseException(messageHelper.get("error.staj.sorumlu.ayni"));
        }
        // ADIM 1 eslesme kurali: yeni sorumlular yeni departmana ait olmali.
        if (!departmanEslesiyor(asil, departman)
                || (yedek != null && !departmanEslesiyor(yedek, departman))) {
            throw new BaseException(messageHelper.get("error.departman.eslesmiyor"));
        }
        // TEK ASIL KURALI: bu stajin kendisi haric (asil ayni kalabilir).
        asilMusaitMiDogrula(asil.getId(), s.getId());

        s.setDepartman(departman);
        s.setAsilSorumlu(asil);
        s.setYedekSorumlu(yedek);
        return toResponse(stajRepository.save(s));
    }

    @Override
    public void delete(Long id) {
        stajRepository.findById(id).ifPresent(s -> {
            s.setStatus(false); // soft delete
            stajRepository.save(s);
        });
    }

    // ---- Gorev 4: yetki izolasyonu ----

    @Override
    public List<StajResponse> benimStajlarim(Long sorumluId) {
        // asil VEYA yedek oldugu tum stajlar (silinenler haric)
        return stajRepository.findByAsilSorumluIdOrYedekSorumluId(sorumluId, sorumluId)
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getStatus()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- ADIM 2 (KISI-B): stajyerin kendi stajlari ----

    @Override
    public List<StajResponse> stajyerStajlarim(Long stajyerId) {
        // Durum filtresi YOK: gecmis donemler de listelenir (salt-okunur gorunur).
        // Ama SILINEN staj gosterilmez: durum ile status ayri seyler, soft-delete
        // durum'a dokunmadigi icin suzulmezse silinen staj listede kalirdi.
        return stajRepository.findByStajyerIdOrderByIdDesc(stajyerId)
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getStatus()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- ORTAK KAPI (KISI-B, ADIM 1): sahiplik + aktiflik ----

    @Override
    public Staj benimAktifStajim(Long stajId, Long sorumluId) {
        Staj s = stajRepository.findById(stajId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")));

        // Silinen staj yok sayilir. Soft-delete YALNIZCA status'u false yapar, durum
        // DEVAM_EDIYOR olarak kalir (bkz. delete()); suzulmezse admin staji sildikten
        // sonra da sorumlu devam/gorev yazmaya devam edebilir, ustelik staj hicbir
        // listede gorunmedigi icin bu kayitlar fark edilmezdi. Ayni kok neden
        // bitisiYaklasanlar sorgusunda daha once duzeltilmisti.
        if (!Boolean.TRUE.equals(s.getStatus())) {
            throw new BaseException(messageHelper.get("error.staj.bulunamadi"));
        }

        boolean asil  = s.getAsilSorumlu() != null
                && s.getAsilSorumlu().getId().equals(sorumluId);
        boolean yedek = s.getYedekSorumlu() != null
                && s.getYedekSorumlu().getId().equals(sorumluId);

        // Yetkisiz erisim -> 403 (A'nin sahiplik ihlali kaliboyla tutarli).
        if (!asil && !yedek) {
            throw new AccessDeniedException(messageHelper.get("error.staj.yetki.yok"));
        }
        // Tamamlanmis/pasif staja yazilamaz -> 400.
        if (s.getDurum() != StajDurumu.DEVAM_EDIYOR) {
            throw new BaseException(messageHelper.get("error.staj.aktif.degil"));
        }
        return s;
    }
}
