package com.example.StajEnvanterProjesi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity(name="devam")

@Table(
        name = "devam",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_devam_staj_tarih",
                columnNames = {"staj_id", "tarih"}
        )
)

public class Devam extends GenericObjectEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staj_id",nullable = false)
    private Staj staj;

    @Column(name = "tarih",nullable = false)
    private LocalDate tarih;

    @Column(name = "giris_saati")
    private LocalDateTime girisSaati;

    @Column(name = "cikis_saati")
    private LocalDateTime cikisSaati;

    @Column(name = "manuel_mi",nullable = false)
    private Boolean manuelMi=false;

    @ManyToOne()
    @JoinColumn(name ="kaydeden_kullanici_id")
    private Kullanici kaydeden;

    /**
     * V15 — cift beyan. Stajyerin kendi bildirdigi kayit false baslar; sorumlu onaylayinca
     * true olur. Sorumlu/admin eliyle girilen kayit dogrudan true (kendi beyanini
     * kendisi onaylamis sayilir).
     * Onaylanmamis gun devam yuzdesine SAYILMAZ.
     */
    @Column(name = "onaylandi", nullable = false)
    private Boolean onaylandi = true;

    @Column(name = "onay_tarihi")
    private LocalDateTime onayTarihi;

    // Iliski degil duz id: Sorumlu/Stajyer'deki olusturanKullaniciId ile ayni kalip.
    // Onayi kim verdi (asil ya da yedek sorumlu olabilir).
    @Column(name = "onay_kullanici_id")
    private Long onaylayanKullaniciId;
}
