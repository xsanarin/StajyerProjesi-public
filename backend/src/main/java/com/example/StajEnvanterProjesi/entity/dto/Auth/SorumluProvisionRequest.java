package com.example.StajEnvanterProjesi.entity.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ADIM 2 — Admin bir SORUMLU olustururken gonderir (profil + login hesabi birlikte).
 */
@Data
public class SorumluProvisionRequest {

    @NotBlank
    private String ad;

    @NotBlank
    private String soyad;

    @NotBlank
    @Email
    private String email;

    private String telefon;

    // ADIM 1: sorumlu departmani (staj eslesme kuralinda kullanilir). hashId olarak gelir.
    private String departmanHashId;
}
