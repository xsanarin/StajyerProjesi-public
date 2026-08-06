package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Auth.LoginRequest;
import com.example.StajEnvanterProjesi.entity.dto.Auth.LoginResponse;
import com.example.StajEnvanterProjesi.entity.dto.Auth.SifreDegistirRequest;
import com.example.StajEnvanterProjesi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ADIM 2 — Kimlik uc noktalari. SecurityConfig'te /api/auth/** permitAll.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/sifre-degistir")
    public ResponseEntity<LoginResponse> sifreDegistir(@Valid @RequestBody SifreDegistirRequest request) {
        return ResponseEntity.ok(authService.sifreDegistir(request));
    }
}
