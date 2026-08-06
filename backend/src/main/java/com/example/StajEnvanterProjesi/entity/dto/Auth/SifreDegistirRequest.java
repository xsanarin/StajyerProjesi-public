package com.example.StajEnvanterProjesi.entity.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ADIM 2 — Sifre degistirme (POST /api/auth/sifre-degistir).
 * Ilk giriste zorunlu akista kullanici henuz tam token almadigi icin kimlik
 * e-posta + eski sifre ile dogrulanir.
 */
@Data
public class SifreDegistirRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String eskiSifre;

    @NotBlank
    @Size(min = 6, message = "Yeni sifre en az 6 karakter olmali")
    private String yeniSifre;
}
