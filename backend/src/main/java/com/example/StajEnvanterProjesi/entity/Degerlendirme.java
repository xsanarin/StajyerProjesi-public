package com.example.StajEnvanterProjesi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * ADIM 4 — Bir stajin puani. Kural: 1 staj = 1 puan.
 * staj_id UNIQUE -> ayni staja iki degerlendirme yazilamaz (DB seviyesinde cift guvence).
 * Puanlama yapilinca Staj.durum = TAMAMLANDI olur ve yazma islemleri kilitlenir (ADIM 4 Bitti).
 */
@Data
@Entity(name = "degerlendirme")
@Table(
        name = "degerlendirme",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_degerlendirme_staj",
                columnNames = "staj_id"
        )
)
public class Degerlendirme extends GenericObjectEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "staj_id", nullable = false, unique = true)
    private Staj staj;

    // 0-100 araligi Service'te dogrulanir.
    @Column(name = "puan", nullable = false)
    private Double puan;

    @Column(name = "yorum", length = 2000)
    private String yorum;

    // Puani veren asil sorumlunun id'si (kim puanladi izi).
    @Column(name = "puanlayan_sorumlu_id")
    private Long puanlayanSorumluId;

    // Admin duzeltme yaparsa gerekce zorunlu (izlenebilirlik). Normal puanlamada null.
    @Column(name = "duzeltme_gerekcesi", length = 2000)
    private String duzeltmeGerekcesi;
}
