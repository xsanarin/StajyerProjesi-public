package com.example.StajEnvanterProjesi.entity.dto.Auth;

import com.example.StajEnvanterProjesi.entity.enums.Rol;
import lombok.Data;

/**
 * ADIM 2 — Provisioning yaniti. geciciSifre YALNIZCA bu response'ta BIR KEZ doner;
 * DB'de sadece BCrypt hash saklanir, duz sifre asla tekrar gosterilmez.
 */
@Data
public class ProvisionResponse {
    private String profilHashId;   // olusan Sorumlu/Stajyer profilinin hashId'si
    private String email;
    private Rol rol;
    private String geciciSifre;    // kullaniciya iletilecek tek seferlik sifre
    private String mesaj;
}
