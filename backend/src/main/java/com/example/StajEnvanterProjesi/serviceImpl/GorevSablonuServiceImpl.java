package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Departman;
import com.example.StajEnvanterProjesi.entity.GorevSablonu;
import com.example.StajEnvanterProjesi.entity.SablonMaddesi;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonResponse;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonMaddeRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonMaddeResponse;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DepartmanRepository;
import com.example.StajEnvanterProjesi.repository.GorevSablonuRepository;
import com.example.StajEnvanterProjesi.service.GorevSablonuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GorevSablonuServiceImpl implements GorevSablonuService {

    private final GorevSablonuRepository sablonRepository;
    private final DepartmanRepository departmanRepository;
    private final MessageHelper messageHelper;

    public GorevSablonuServiceImpl(GorevSablonuRepository sablonRepository,
                                   DepartmanRepository departmanRepository,
                                   MessageHelper messageHelper) {
        this.sablonRepository = sablonRepository;
        this.departmanRepository = departmanRepository;
        this.messageHelper = messageHelper;
    }

    @Override
    @Transactional
    public SablonResponse olustur(SablonRequest request) {
        GorevSablonu sablon = new GorevSablonu();
        sablon.setAktif(true);
        uygula(sablon, request);
        return toResponse(sablonRepository.save(sablon));
    }

    @Override
    @Transactional
    public SablonResponse guncelle(String hashId, SablonRequest request) {
        GorevSablonu sablon = sablonBul(hashId);

        // Maddeler tumuyle yenilenir (orphanRemoval eskileri siler). Bu, ATANMIS
        // gorevleri etkilemez: staj_gorev_madde.madde_metni bagimsiz bir kopyadir.
        //
        // ARADAKI saveAndFlush SART: clear() + yeni ekleme ayni flush'ta olursa
        // Hibernate yeni satirlari orphan DELETE'lerden ONCE INSERT eder ve
        // uq_sablon_maddesi_sira (sablon_id, sira) çakisir — guncelleme 500 verir.
        // Flush, silmeleri yeni eklemelerden once DB'ye yazar.
        // Metod @Transactional oldugu icin sonradan bir dogrulama patlarsa
        // silmeler de geri alinir.
        sablon.getMaddeler().clear();
        sablonRepository.saveAndFlush(sablon);

        uygula(sablon, request);
        return toResponse(sablonRepository.save(sablon));
    }

    @Override
    @Transactional
    public void pasiflestir(String hashId) {
        GorevSablonu sablon = sablonBul(hashId);
        sablon.setAktif(false);
        sablonRepository.save(sablon);
    }

    @Override
    public SablonResponse getir(String hashId) {
        return toResponse(sablonBul(hashId));
    }

    @Override
    public List<SablonResponse> listele(boolean sadeceAktif, String departmanHashId) {
        List<GorevSablonu> liste;
        if (departmanHashId == null || departmanHashId.isBlank()) {
            liste = sadeceAktif ? sablonRepository.findByAktifTrue() : sablonRepository.findAll();
        } else {
            Long departmanId = departmanRepository.findByHashId(departmanHashId)
                    .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")))
                    .getId();
            liste = sadeceAktif
                    ? sablonRepository.findByDepartmanIdAndAktifTrue(departmanId)
                    : sablonRepository.findByDepartmanId(departmanId);
        }
        return liste.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ---- yardimcilar ----

    private GorevSablonu sablonBul(String hashId) {
        return sablonRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.sablon.bulunamadi")));
    }

    /** Request alanlarini entity'ye yazar; madde siralarini normalize eder. */
    private void uygula(GorevSablonu sablon, SablonRequest request) {
        Departman departman = departmanRepository.findByHashId(request.getDepartmanHashId())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")));

        sablon.setAd(request.getAd());
        sablon.setAciklama(request.getAciklama());
        sablon.setDepartman(departman);
        sablon.setSeviye(request.getSeviye());

        // Sira verilmezse listedeki konumdan uretilir. uq_sablon_maddesi_sira
        // mukerrer sirayi DB'de de engelliyor; burada kendi icinde de dogruluyoruz.
        int sonraki = 1;
        for (SablonMaddeRequest maddeReq : request.getMaddeler()) {
            SablonMaddesi madde = new SablonMaddesi();
            madde.setMaddeMetni(maddeReq.getMaddeMetni());
            madde.setSira(maddeReq.getSira() != null ? maddeReq.getSira() : sonraki);
            sablon.maddeEkle(madde);
            sonraki++;
        }

        long farkliSira = sablon.getMaddeler().stream().map(SablonMaddesi::getSira).distinct().count();
        if (farkliSira != sablon.getMaddeler().size()) {
            throw new BaseException(messageHelper.get("error.sablon.sira.tekrar"));
        }
    }

    private SablonResponse toResponse(GorevSablonu s) {
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
