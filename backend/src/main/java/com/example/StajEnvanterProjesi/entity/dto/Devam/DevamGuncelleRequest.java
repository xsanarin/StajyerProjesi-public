package com.example.StajEnvanterProjesi.entity.dto.Devam;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Admin devam duzeltme: sadece giris/cikis saati degistirilir.
 * Tarih ve staj DEGISMEZ (yanlis gun -> sil + yeniden manuel ekle).
 * Kurallar serviste: cikis varsa giris zorunlu, cikis giristen once olamaz.
 */
@Data
public class DevamGuncelleRequest {

    private LocalDateTime girisSaati;

    private LocalDateTime cikisSaati;
}