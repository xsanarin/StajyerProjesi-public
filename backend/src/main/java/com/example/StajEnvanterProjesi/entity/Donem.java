package com.example.StajEnvanterProjesi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Staj donemi (orn. "2026 Yaz", "2026-2027 Guz").
 * Plan Gorev 5: Tum operasyonel veri staj_id uzerinden baglanir; donem burada tutulur.
 * Kritik kural: ayni anda yalnizca 1 donem "aktif" olabilir.
 *   -> Bu tekillik Service katmaninda kontrol edilir (ileride DB'de partial unique index ile de saglanabilir).
 */
@Data
@Entity(name = "donem")
@Table(name = "donem")
public class Donem extends GenericObjectEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad", nullable = false)
    private String ad;                 // Ekranda gorunen isim, orn "2026 Yaz Donemi"

    @Column(name = "baslangic_tarihi")
    private LocalDate baslangicTarihi;

    @Column(name = "bitis_tarihi")
    private LocalDate bitisTarihi;

    @Column(name = "aktif", nullable = false)
    private Boolean aktif = false;     // Yalnizca bir donem true olabilir (Service'te dogrulanir)
}
