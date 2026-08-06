package com.example.StajEnvanterProjesi.entity.dto.Talep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * BT talebi olusturma/guncelleme istegi (yalnizca ADMIN).
 * Departman disariya hashId ile verilir (DTO kurali - ADIM 0).
 */
@Data
public class TalepRequest {

    @NotBlank
    private String departmanHashId;

    /**
     * Talebin acildigi stajyer — OPSIYONEL (V22). Bos/null gelirse talep
     * departman geneline acilmis sayilir ve mevcut kayit varsa atama temizlenir
     * (PUT tam ezme semantigi).
     *
     * Verilirse stajyerin secilen departmanda aktif staji olmak zorunda; kontrol
     * TalepServiceImpl'de.
     */
    private String stajyerHashId;

    @NotBlank
    @Size(max = 200)
    private String baslik;

    @Size(max = 4000)
    private String aciklama;
}
