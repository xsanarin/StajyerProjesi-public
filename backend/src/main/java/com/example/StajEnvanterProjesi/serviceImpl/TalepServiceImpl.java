package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Departman;
import com.example.StajEnvanterProjesi.entity.Sorumlu;
import com.example.StajEnvanterProjesi.entity.Stajyer;
import com.example.StajEnvanterProjesi.entity.Talep;
import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerResponse;
import com.example.StajEnvanterProjesi.entity.dto.Talep.TalepRequest;
import com.example.StajEnvanterProjesi.entity.dto.Talep.TalepResponse;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DepartmanRepository;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.repository.StajyerRepository;
import com.example.StajEnvanterProjesi.repository.TalepRepository;
import com.example.StajEnvanterProjesi.service.TalepService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BT talep CRUD. Departman/Donem CRUD kalibini izler.
 *
 * Talep departmana baglidir; sorumlu kendi departmaninin taleplerini gorur.
 */
@Service
public class TalepServiceImpl implements TalepService {

    /**
     * Talep yalnizca staji DEVAM EDEN stajyere acilabilir (urun karari).
     *
     * Tek yerde duruyor cunku iki sorguya birden veriliyor — secim listesi ve
     * kaydetme dogrulamasi. Ayri ayri yazilsa biri degisip digeri unutuldugunda
     * dialogda gorunen kisi kaydederken reddedilirdi.
     */
    private static final StajDurumu TALEP_ICIN_GECERLI_DURUM = StajDurumu.DEVAM_EDIYOR;

    private final TalepRepository talepRepository;
    private final DepartmanRepository departmanRepository;
    // Sorumlunun departmanini cozmek icin: talep kisiye degil departmana bagli.
    private final SorumluRepository sorumluRepository;
    // V22 - stajyer atamasi: kisiyi bulmak ve departmana aitligini dogrulamak icin.
    private final StajyerRepository stajyerRepository;
    private final StajRepository stajRepository;
    private final MessageHelper messageHelper;

    public TalepServiceImpl(TalepRepository talepRepository,
                            DepartmanRepository departmanRepository,
                            SorumluRepository sorumluRepository,
                            StajyerRepository stajyerRepository,
                            StajRepository stajRepository,
                            MessageHelper messageHelper) {
        this.talepRepository = talepRepository;
        this.departmanRepository = departmanRepository;
        this.sorumluRepository = sorumluRepository;
        this.stajyerRepository = stajyerRepository;
        this.stajRepository = stajRepository;
        this.messageHelper = messageHelper;
    }

    // ---- Cevirici ----

    private TalepResponse toResponse(Talep t) {
        TalepResponse r = new TalepResponse();
        r.setHashId(t.getHashId());
        r.setBaslik(t.getBaslik());
        r.setAciklama(t.getAciklama());
        r.setOlusturmaTarihi(t.getCreateDate());
        if (t.getDepartman() != null) {
            r.setDepartmanHashId(t.getDepartman().getHashId());
            r.setDepartmanAd(t.getDepartman().getAd());
        }
        // Atanmamis talepte ikisi de null kalir; ekranda "-" gosterilir.
        if (t.getStajyer() != null) {
            r.setStajyerHashId(t.getStajyer().getHashId());
            r.setStajyerAdSoyad(t.getStajyer().getAd() + " " + t.getStajyer().getSoyad());
        }
        return r;
    }

    private StajyerResponse toStajyerResponse(Stajyer s) {
        StajyerResponse r = new StajyerResponse();
        r.setHashId(s.getHashId());
        r.setAd(s.getAd());
        r.setSoyad(s.getSoyad());
        r.setEmail(s.getEmail());
        r.setTelefon(s.getTelefon());
        r.setUniversite(s.getUniversite());
        r.setBolum(s.getBolum());
        r.setSinif(s.getSinif());
        r.setStatus(s.getStatus());
        return r;
    }

    // ---- Admin islemleri ----

    @Override
    @Transactional
    public TalepResponse olustur(TalepRequest request) {
        Talep t = new Talep();
        Departman departman = departmanBul(request.getDepartmanHashId());
        t.setDepartman(departman);
        t.setStajyer(stajyerCoz(request.getStajyerHashId(), departman));
        t.setBaslik(request.getBaslik());
        t.setAciklama(request.getAciklama());
        return toResponse(talepRepository.save(t));
    }

    @Override
    @Transactional
    public TalepResponse guncelle(String hashId, TalepRequest request) {
        Talep t = talepBul(hashId);
        // Departman degistirilebilir: talep yanlis departmana acildiysa tasinabilmeli.
        Departman departman = departmanBul(request.getDepartmanHashId());
        t.setDepartman(departman);
        // Atama YENI departmana gore dogrulanir: talep baska departmana tasinirken
        // eski departmanin stajyeri uzerinde kalamaz — istek ya o departmandan bir
        // kisi tasir ya da bos gelir (bosalt).
        t.setStajyer(stajyerCoz(request.getStajyerHashId(), departman));
        t.setBaslik(request.getBaslik());
        t.setAciklama(request.getAciklama());
        return toResponse(talepRepository.save(t));
    }

    @Override
    @Transactional
    public void sil(String hashId) {
        Talep t = talepBul(hashId);
        t.setStatus(false);
        talepRepository.save(t);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TalepResponse> listele() {
        return talepRepository.findByStatusTrueOrderByIdDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- Ortak okuma ----

    @Override
    @Transactional(readOnly = true)
    public TalepResponse getir(String hashId) {
        return toResponse(talepBul(hashId));
    }

    // ---- Sorumlu ----

    @Override
    @Transactional(readOnly = true)
    public List<TalepResponse> sorumluListele(Long sorumluId) {
        Sorumlu sorumlu = sorumluRepository.findById(sorumluId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.sorumlu.bulunamadi")));

        // Departmani olmayan sorumlu hata degil, gecerli bir ara durum: Sorumlu.departman
        // nullable (provisioning oncesi kayitlar). Bos liste donmek dogru cevap.
        if (sorumlu.getDepartman() == null) {
            return Collections.emptyList();
        }

        return talepRepository
                .findByDepartmanIdAndStatusTrueOrderByIdDesc(sorumlu.getDepartman().getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- Secim listesi ----

    @Override
    @Transactional(readOnly = true)
    public List<StajyerResponse> departmanStajyerleri(String departmanHashId) {
        Departman departman = departmanBul(departmanHashId);
        return stajRepository.departmanStajyerleri(departman.getId(), TALEP_ICIN_GECERLI_DURUM)
                .stream()
                .map(this::toStajyerResponse)
                .collect(Collectors.toList());
    }

    // ---- Yardimcilar ----

    /**
     * Istekteki stajyer hashId'sini entity'ye cevirir; bos ise null doner (atama yok).
     *
     * Departman kontrolu burada, istemciye guvenilmedigi icin: dialog listeyi
     * departmana gore suzuyor ama istek elle degistirilebilir ve o durumda talep
     * hicbir sorumlusunun goremeyecegi bir kisiye baglanirdi.
     */
    private Stajyer stajyerCoz(String stajyerHashId, Departman departman) {
        if (stajyerHashId == null || stajyerHashId.isBlank()) {
            return null;
        }

        Stajyer stajyer = stajyerRepository.findByHashId(stajyerHashId)
                .filter(s -> Boolean.TRUE.equals(s.getStatus()))
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.stajyer.bulunamadi")));

        boolean departmandaCalisiyor = stajRepository.existsByStajyerIdAndDepartmanIdAndDurumAndStatusTrue(
                stajyer.getId(), departman.getId(), TALEP_ICIN_GECERLI_DURUM);
        if (!departmandaCalisiyor) {
            throw new BaseException(messageHelper.get("error.talep.stajyer.departman.uyusmuyor"));
        }
        return stajyer;
    }

    private Talep talepBul(String hashId) {
        return talepRepository.findByHashIdAndStatusTrue(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.talep.bulunamadi")));
    }

    private Departman departmanBul(String hashId) {
        return departmanRepository.findByHashIdAndStatusTrue(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")));
    }
}
