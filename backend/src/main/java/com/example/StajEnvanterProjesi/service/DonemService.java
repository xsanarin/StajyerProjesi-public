package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Donem.DonemRequest;
import com.example.StajEnvanterProjesi.entity.dto.Donem.DonemResponse;

import java.util.List;

/**
 * Donem is kurallarinin sozlesmesi (arayuz).
 * Controller sadece bu arayuzu bilir; gerçek is DonemServiceImpl'de yapilir.
 */
public interface DonemService {

    DonemResponse findById(Long id);

    List<DonemResponse> findAll();

    DonemResponse save(DonemRequest request);

    DonemResponse update(Long id, DonemRequest request);

    void delete(Long id);

    // Bir donemi "aktif" yapar; diger tum donemleri pasife ceker (tek aktif donem kurali).
    DonemResponse aktifYap(Long id);
}
