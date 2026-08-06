package com.example.StajEnvanterProjesi.entity.dto.Degerlendirme;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ADIM 4 — Admin puan duzeltme (PUT /api/admin/stajlar/{hashId}/puan).
 * gerekce ZORUNLU: puan neden degistirildi izlenebilir olsun.
 */
@Data
public class PuanDuzeltRequest {

    @NotNull
    @DecimalMin(value = "0", message = "Puan 0'dan kucuk olamaz")
    @DecimalMax(value = "100", message = "Puan 100'den buyuk olamaz")
    private Double puan;

    private String yorum;

    @NotBlank(message = "Duzeltme gerekcesi zorunlu")
    private String gerekce;
}
