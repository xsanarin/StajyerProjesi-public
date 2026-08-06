package com.example.StajEnvanterProjesi.security;

import com.example.StajEnvanterProjesi.entity.enums.Rol;

/**
 * ADIM 2 — SecurityContext'e konan kimlik. Controller/Service, giris yapan kullaniciyi
 * parametre yerine buradan (token'dan) okur. sorumluId/stajyerId role gore dolu olur.
 * email audit/log icin tutulur (kim ekledi/sildi okunabilir gorunsun).
 */
public record AuthPrincipal(Long kullaniciId, String email, Rol rol, Long sorumluId, Long stajyerId) {
}