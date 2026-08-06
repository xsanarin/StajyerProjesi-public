package com.example.StajEnvanterProjesi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * BT (Bilgi Teknolojileri) talebi.
 *
 * KRITIK KARAR: talep DEPARTMANA baglidir, sorumluya DEGIL. Bir departmanda birden
 * fazla sorumlu olabilir ve hepsi ayni talep listesini gorur (ortak liste). "Sorumlunun
 * kendi talepleri" demek "kendi departmaninin talepleri" demektir; bu yuzden burada
 * sorumlu FK'si yoktur, suzme Sorumlu.departman uzerinden yapilir.
 *
 * Bu ilk surumde talep tek yonludur: ADMIN olusturur, SORUMLU okur. Durum akisi,
 * not gecmisi ve oncelik bilerek disarida birakildi (bkz. V21 migration yorumu).
 */
@Data
@Entity(name = "talep")
@Table(name = "talep")
public class Talep extends GenericObjectEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "departman_id", nullable = false)
    private Departman departman;

    /**
     * Talebin acildigi stajyer (V22). NULL olabilir: departman geneline acilan
     * talepler kimseye ait degildir.
     *
     * Stajyer'in kendi departman alani yok — departman staja baglidir. Bu yuzden
     * "bu stajyer bu departmanda mi" kurali TalepServiceImpl'de, atanan kisinin
     * secilen departmanda DEVAM EDEN bir staji oldugu dogrulanarak isletilir:
     * talep acilan kisi o an departmanda fiilen calisiyor olmali.
     */
    @ManyToOne
    @JoinColumn(name = "stajyer_id")
    private Stajyer stajyer;

    @Column(name = "baslik", nullable = false, length = 200)
    private String baslik;

    @Column(name = "aciklama", length = 4000)
    private String aciklama;
}
