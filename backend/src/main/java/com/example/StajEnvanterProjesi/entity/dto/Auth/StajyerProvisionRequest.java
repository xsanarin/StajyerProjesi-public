package com.example.StajEnvanterProjesi.entity.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ADIM 2 — Admin bir STAJYER olustururken gonderir (profil + login hesabi birlikte).
 * Stajyer saf kisi tablosudur (Karar 1): departman/tarih burada YOK, staj acilirken belirlenir.
 */
@Data
public class StajyerProvisionRequest {

    @NotBlank
    private String ad;

    @NotBlank
    private String soyad;

    @NotBlank
    @Email
    private String email;

    private String telefon;
    private String universite;
    private String bolum;
    private Integer sinif;
}
