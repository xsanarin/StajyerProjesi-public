package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ADIM 2 — Staja sablon atama girisi. Kurallar servis katmaninda:
 * sablon aktif olmali, departman ve seviye staj ile uyusmali.
 */
@Data
public class SablonAtaRequest {

    @NotBlank
    private String sablonHashId;
}
