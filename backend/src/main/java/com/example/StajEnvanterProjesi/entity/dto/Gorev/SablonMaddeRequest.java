package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ADIM 2 — Sablona madde girisi.
 * sira verilmezse servis mevcut en buyuk siranin bir fazlasini atar.
 */
@Data
public class SablonMaddeRequest {

    @NotBlank
    @Size(max = 2000)
    private String maddeMetni;

    private Integer sira;
}
