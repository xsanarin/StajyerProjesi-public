package com.example.StajEnvanterProjesi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(name = "sorumlu")
@Table(name = "sorumlu")
public class Sorumlu extends GenericObjectEntity implements Serializable {
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

    // ADIM 1: Sorumlu bir departmana baglidir (Java, .NET, Database...).
    // Staj acilirken stajyerin/asil/yedek sorumlunun departmani ayni olmali (eslesme kurali).
    // nullable: mevcut kayitlar ve auth/provisioning gelmeden once bos olabilir.
    @ManyToOne
    @JoinColumn(name = "departman_id")
    private Departman departman;

    // --- Denetim (V14): kaydi kim ekledi / kim (soft) sildi ---
    // Giris yapan kullanicinin id'si (kullanici.id). HTTP disi akislarda null kalabilir.
    @Column(name = "olusturan_kullanici_id")
    private Long olusturanKullaniciId;

    @Column(name = "silen_kullanici_id")
    private Long silenKullaniciId;

    @Column(name = "silme_tarihi")
    private LocalDateTime silmeTarihi;
}
