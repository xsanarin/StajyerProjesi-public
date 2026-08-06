package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Auth.ProvisionResponse;
import com.example.StajEnvanterProjesi.entity.dto.Auth.SorumluProvisionRequest;
import com.example.StajEnvanterProjesi.entity.dto.Auth.StajyerProvisionRequest;

/**
 * ADIM 2 — Admin provisioning sozlesmesi: profil + login hesabini tek islemde olusturur.
 */
public interface ProvisioningService {

    ProvisionResponse sorumluOlustur(SorumluProvisionRequest request);

    ProvisionResponse stajyerOlustur(StajyerProvisionRequest request);
}
