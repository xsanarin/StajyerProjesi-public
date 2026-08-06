package com.example.StajEnvanterProjesi.entity.dto.Auth;

import com.example.StajEnvanterProjesi.entity.enums.Rol;
import lombok.Data;

/**
 * ADIM 2 — Login yaniti.
 * sifreDegistirmeli=true ise token BOS doner; istemci once /api/auth/sifre-degistir'e gider.
 */
@Data
public class LoginResponse {
    private String token;                 // sifre degisimi gerekiyorsa null
    private Rol rol;
    private boolean sifreDegistirmeli;
    private String mesaj;

    public static LoginResponse token(String token, Rol rol) {
        LoginResponse r = new LoginResponse();
        r.setToken(token);
        r.setRol(rol);
        r.setSifreDegistirmeli(false);
        return r;
    }

    public static LoginResponse sifreDegistir(Rol rol, String mesaj) {
        LoginResponse r = new LoginResponse();
        r.setRol(rol);
        r.setSifreDegistirmeli(true);
        r.setMesaj(mesaj);
        return r;
    }
}
