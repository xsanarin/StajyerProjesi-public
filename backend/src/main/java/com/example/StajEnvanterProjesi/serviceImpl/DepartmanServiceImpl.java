package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Departman;
import com.example.StajEnvanterProjesi.entity.dto.Departman.DepartmanRequest;
import com.example.StajEnvanterProjesi.entity.dto.Departman.DepartmanResponse;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DepartmanRepository;
import com.example.StajEnvanterProjesi.service.DepartmanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ADIM 1 — Departman CRUD (Donem paterni ile ayni).
 * Response'ta id yaninda hashId de doner (DTO kurali - ADIM 0).
 */
@Service
public class







DepartmanServiceImpl implements DepartmanService {

    @Autowired
    DepartmanRepository departmanRepository;

    @Autowired
    MessageHelper messageHelper;

    // ---- Cevirici (mapper) ----

    private DepartmanResponse toResponse(Departman departman) {
        if (departman == null) return null;
        DepartmanResponse r = new DepartmanResponse();
        r.setHashId(departman.getHashId());
        r.setAd(departman.getAd());
        r.setStatus(departman.getStatus());
        return r;
    }

    // ---- CRUD ----

    @Override
    public DepartmanResponse findById(Long id) {
        return departmanRepository.findById(id).map(this::toResponse).orElse(null);
    }

    @Override
    public List<DepartmanResponse> findAll() {
        // Silinen (status=false) departman listede ve secim kutularinda gorunmemeli.
        return departmanRepository.findByStatusTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmanResponse save(DepartmanRequest request) {
        // Ad tekilligi (DB'de unique; kullaniciya anlamli mesaj icin onceden kontrol).
        if (departmanRepository.existsByAd(request.getAd())) {
            throw new BaseException(messageHelper.get("error.departman.ad.mevcut"));
        }
        Departman d = new Departman();
        d.setAd(request.getAd());
        return toResponse(departmanRepository.save(d));
    }

    @Override
    public DepartmanResponse update(Long id, DepartmanRequest request) {
        Optional<Departman> mevcut = departmanRepository.findById(id);
        if (mevcut.isEmpty()) return null;

        Departman d = mevcut.get();
        // Ad degisiyorsa ve yeni ad baska bir kayitta varsa engelle.
        if (!d.getAd().equals(request.getAd()) && departmanRepository.existsByAd(request.getAd())) {
            throw new BaseException(messageHelper.get("error.departman.ad.mevcut"));
        }
        d.setAd(request.getAd());
        return toResponse(departmanRepository.save(d));
    }

    @Override
    public void delete(Long id) {
        // Soft delete: GenericObjectEntity.status = false (diger modullerle ayni patern).
        departmanRepository.findById(id).ifPresent(d -> {
            d.setStatus(false);
            departmanRepository.save(d);
        });
    }
}
