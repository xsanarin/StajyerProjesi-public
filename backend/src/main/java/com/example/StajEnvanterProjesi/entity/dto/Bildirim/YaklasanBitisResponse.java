package com.example.StajEnvanterProjesi.entity.dto.Bildirim;

import lombok.Data;

import java.time.LocalDate;

/**
 * ADIM 3 (KISI-B) — Bitisi yaklasan staj satiri.
 *
 * A'nin sorumlu ekranindaki "yaklasan bitisler" kutusu bu yaniti kullanir
 * (KISI-B.md: "endpoint'i sen ver, ekrani A yapar").
 *
 * gunKalan 0 ise staj BUGUN bitiyor; negatif olmaz (sorgu bugunden ileriye bakar).
 */
@Data
public class YaklasanBitisResponse {

    private String stajHashId;
    private String stajyerAdSoyad;
    private String departmanAd;
    private LocalDate stajBitisTarihi;
    private long gunKalan;

    // Kimin sorumlulugunda? Admin gorunumunde kime haber verilecegini gosterir.
    private String asilSorumluAdSoyad;
    private String yedekSorumluAdSoyad;
}
