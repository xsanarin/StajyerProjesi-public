package com.example.StajEnvanterProjesi.entity.dto.Staj;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Acilmis stajin duzeltilmesi (admin): tarihler, haftalik devam plani ve asil sorumlu.
 *
 * KISMI GUNCELLEME: null gelen alan degistirilmez. Tarihler stajta hicbir zaman null
 * olmamali (acilista donemden devralinir), o yuzden "null = temizle" anlami YOK.
 *
 * Departman burada degismez — departman degisince ADIM 1 eslesme kurali geregi asil ve
 * yedek birlikte degismek zorunda, o is {@link DepartmanDegistirRequest} ile yapilir.
 * Yedek sorumlu da ayri uctan (yedekSorumluAta) yonetilir.
 */
@Data
public class StajGuncelleRequest {

    private LocalDate stajBaslangicTarihi;

    private LocalDate stajBitisTarihi;

    /** Bos kume gonderilemez: devam plani olmayan stajda takvim uretilemez. */
    private Set<DayOfWeek> devamGunleri;

    private String asilSorumluHashId;
}
