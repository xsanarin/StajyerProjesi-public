package com.example.StajEnvanterProjesi.entity.dto.Donem;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * Disaridan (Admin) donem olustururken/guncellerken gelen veri.
 * Not: id ve aktiflik gibi alanlar burada tutulmaz; onlari sunucu yonetir.
 */
@Data
public class DonemRequest {
    @NotBlank
    private String ad;
    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;
}
