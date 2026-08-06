package com.example.StajEnvanterProjesi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ADIM 0 — Stajyer artik SAF KISI tablosudur (Karar 1).
 * Donem'e ozel veriler (staj tarihleri, durum) buradan kaldirilip Staj'a tasindi;
 * boylece ayni kisi farkli donemlerde staj yapinca veriler ust uste binmez.
 */
@Data
@Entity(name = "stajyer")
@Table(name = "stajyer")
public class Stajyer extends GenericObjectEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(name = "ad", nullable = false)
    private String ad;

    @Column(name = "soyad", nullable = false)
    private String soyad;

    @Column(name = "email")
    private String email;

    @Column(name = "telefon")
    private String telefon;

    @Column(name = "universite")
    private String universite;

    @Column(name = "bolum")
    private String bolum;

    @Column(name = "sinif")
    private Integer sinif;

    // --- Denetim (V14): kaydi kim ekledi / kim (soft) sildi ---
    // Giris yapan kullanicinin id'si (kullanici.id). HTTP disi akislarda null kalabilir.
    @Column(name = "olusturan_kullanici_id")
    private Long olusturanKullaniciId;

    @Column(name = "silen_kullanici_id")
    private Long silenKullaniciId;

    @Column(name = "silme_tarihi")
    private LocalDateTime silmeTarihi;

    // NOT: stajBaslangicTarihi/stajBitisTarihi/durum ADIM 0'da Staj'a tasindi.
    // status (soft delete) GenericObjectEntity'den geldigi icin burada tekrar tanimlanmaz.
}

