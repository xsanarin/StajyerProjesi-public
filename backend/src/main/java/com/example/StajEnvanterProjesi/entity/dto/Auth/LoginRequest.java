package com.example.StajEnvanterProjesi.entity.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ADIM 2 — Login istegi (POST /api/auth/login).
 */
@Data
public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String sifre;
}
