package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Donem;
import com.example.StajEnvanterProjesi.entity.dto.Donem.DonemRequest;
import com.example.StajEnvanterProjesi.entity.dto.Donem.DonemResponse;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DonemRepository;
import com.example.StajEnvanterProjesi.service.DonemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DonemServiceImpl implements DonemService {

    /** Donem adi sabit: staj donemleri bunlarla sinirli, serbest metin degil. */
    public static final Set<String> GECERLI_ADLAR =
            Set.of("Yaz", "Güz", "Bahar");

    @Autowired
    DonemRepository donemRepository;

    @Autowired
    MessageHelper messageHelper;

    // ---- Cevirici (mapper) metotlar: entity <-> dto ----

    private DonemResponse toResponse(Donem donem) {
        if (donem == null) return null;
        DonemResponse r = new DonemResponse();
        r.setHashId(donem.getHashId());
        r.setAd(donem.getAd());
        r.setBaslangicTarihi(donem.getBaslangicTarihi());
        r.setBitisTarihi(donem.getBitisTarihi());
        r.setAktif(donem.getAktif());
        return r;
    }

    private Donem toEntity(DonemRequest request) {
        if (request == null) return null;
        Donem d = new Donem();
        d.setAd(request.getAd());
        d.setBaslangicTarihi(request.getBaslangicTarihi());
        d.setBitisTarihi(request.getBitisTarihi());
        d.setAktif(false); // Yeni donem varsayilan pasif; aktiflik ayri adimda verilir.
        return d;
    }

    /**
     * Donem adi serbest metin degil, sabit uclu kume. Onceden her sey yazilabildigi
     * icin veritabani "D-9675", "BDonem-30225" gibi kayitlarla dolmustu.
     * Ayrica ayni ad iki kez acilamaz: uc ad = en fazla uc donem.
     *
     * @param mevcutId guncellemede kendi kaydini "zaten var" saymamak icin (yeni kayitta null)
     */
    private void adDogrula(String ad, Long mevcutId) {
        if (!GECERLI_ADLAR.contains(ad)) {
            throw new BaseException(messageHelper.get("error.donem.ad.gecersiz"));
        }
        donemRepository.findByAdAndStatusTrue(ad).ifPresent(baskasi -> {
            if (!baskasi.getId().equals(mevcutId)) {
                throw new BaseException(messageHelper.get("error.donem.ad.mevcut", ad));
            }
        });
    }

    // ---- CRUD ----

    @Override
    public DonemResponse findById(Long id) {
        return donemRepository.findById(id).map(this::toResponse).orElse(null);
    }

    @Override
    public List<DonemResponse> findAll() {
        // Silinen (status=false) donem listede ve secim kutularinda gorunmemeli.
        return donemRepository.findByStatusTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DonemResponse save(DonemRequest request) {
        adDogrula(request.getAd(), null);
        Donem kaydedilen = donemRepository.save(toEntity(request));
        return toResponse(kaydedilen);
    }

    @Override
    public DonemResponse update(Long id, DonemRequest request) {
        Optional<Donem> mevcut = donemRepository.findById(id);
        if (mevcut.isEmpty()) return null;

        adDogrula(request.getAd(), id);
        Donem d = mevcut.get();
        d.setAd(request.getAd());
        d.setBaslangicTarihi(request.getBaslangicTarihi());
        d.setBitisTarihi(request.getBitisTarihi());
        return toResponse(donemRepository.save(d));
    }

    @Override
    public void delete(Long id) {
        // Soft delete: GenericObjectEntity.status = false (Sorumlu paterni ile ayni)
        donemRepository.findById(id).ifPresent(d -> {
            d.setStatus(false);
            donemRepository.save(d);
        });
    }

    /**
     * Tek aktif donem kurali burada uygulanir.
     * @Transactional: iki adim (eskiyi kapat + yeniyi ac) tek islemde ya hep ya hic olsun.
     */
    @Override
    @Transactional
    public DonemResponse aktifYap(Long id) {
        // 1) Su an aktif olan donem varsa pasife cek
        donemRepository.findByAktifTrue().ifPresent(mevcutAktif -> {
            mevcutAktif.setAktif(false);
            donemRepository.save(mevcutAktif);
        });

        // 2) Istenen donemi aktif yap
        Optional<Donem> hedef = donemRepository.findById(id);
        if (hedef.isEmpty()) return null;

        Donem d = hedef.get();
        d.setAktif(true);
        return toResponse(donemRepository.save(d));
    }
}
