package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Sorumlu.SorumluRequest;
import com.example.StajEnvanterProjesi.entity.dto.Sorumlu.SorumluResponse;
import java.util.List;



public interface SorumluService {

        SorumluResponse findById(Long id);

        List<SorumluResponse> findAll();

        // Sorumlu olusturma ProvisioningService'te: profil + Kullanici tek transaction'da.
        // Buradaki eski save() login hesabi yaratmadigi icin kaldirildi (ADIM 2).

        SorumluResponse update(Long id, SorumluRequest request);

        void delete(Long id);

        /**
         * ADIM 3 — Sorumlu ayrilma/terfi transaction'i.
         * Tek islemde: asil oldugu DEVAM_EDIYOR stajlarda yedek asile terfi eder (yedeksiz
         * stajyeri varsa hata), yedek oldugu stajlarda slot bosalir, profil soft-delete edilir
         * ve login hesabi kilitlenir.
         */
        void sorumluAyril(Long sorumluId);

}
