package com.example.StajEnvanterProjesi.entity.dto.Degerlendirme;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ADIM 4 — Asil sorumlunun puanlama istegi (POST /api/sorumlu/stajlar/{hashId}/puanla).
 */
@Data
public class PuanRequest {

    @NotNull
    @DecimalMin(value = "0", message = "Puan 0'dan kucuk olamaz")
    @DecimalMax(value = "100", message = "Puan 100'den buyuk olamaz")
    private Double puan;

    private String yorum;
}
