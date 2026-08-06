package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Auth.LoginRequest;
import com.example.StajEnvanterProjesi.entity.dto.Auth.LoginResponse;
import com.example.StajEnvanterProjesi.entity.dto.Auth.SifreDegistirRequest;

/**
 * ADIM 2 — Kimlik dogrulama sozlesmesi.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse sifreDegistir(SifreDegistirRequest request);
}
