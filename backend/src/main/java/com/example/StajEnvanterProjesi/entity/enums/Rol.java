package com.example.StajEnvanterProjesi.entity.enums;

/**
 * ADIM 2 — Kullanici rolleri.
 * Spring Security'de yetki adi "ROLE_" + isim olarak verilir (orn. ROLE_ADMIN),
 * boylece SecurityConfig'teki hasRole("ADMIN") calisir.
 */
public enum Rol {
    ADMIN,
    SORUMLU,
    STAJYER
}
