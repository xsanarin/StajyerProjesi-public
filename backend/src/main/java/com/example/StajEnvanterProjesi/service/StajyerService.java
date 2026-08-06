package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.Stajyer;
import java.util.List;

import com.example.StajEnvanterProjesi.entity.dto.SayfaResponse;
import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerResponse;
import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerRequest;
import org.springframework.stereotype.Service;

@Service
public interface StajyerService {

    StajyerResponse findById(Long id);

    /**
     * DIKKAT: sayfalama yok, tum stajyerleri dondurur.
     * Liste ekranlari ara(...) kullanmali; bu metod staj acma dialogundaki
     * acilir liste gibi kucuk ic kullanimlar icin duruyor.
     */
    List<StajyerResponse> findAll();

    /** Stajyer listesinin sayfali + aramali okuma yolu. Bos terim = filtre yok. */
    SayfaResponse<StajyerResponse> ara(String q, int sayfa, int boyut);

    StajyerResponse save(StajyerRequest request);

    StajyerResponse update(Long id, StajyerRequest request);

    void delete(Long id);


}
