package com.example.StajEnvanterProjesi.entity;

import com.example.StajEnvanterProjesi.entity.enums.GorevDurumu;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ADIM 2 — Staja atanmis gorev maddesi. SNAPSHOT'in tutuldugu yer.
 *
 * Bu bir SORU degil, YAPILACAK IS: "JVM nedir, ne ise yarar" gibi. Stajyer isi
 * yapip tamamlandi isaretler, sorumlu onaylar. stajyerNotu opsiyoneldir —
 * stajyer isterse ne yaptigini yazar, yazmadan da tamamlandi isaretleyebilir.
 *
 * maddeMetni bir FK degil, bagimsiz metin KOPYASIDIR. Sablondaki madde sonradan
 * degistirilse ya da silinse bile buradaki metin degismez — eski stajyerin
 * gorevleri sabit kalir (Gereksinim 3).
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "staj_gorev_madde")
@Table(name = "staj_gorev_madde")
public class StajGorevMaddesi extends GenericObjectEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "staj_gorev_id", nullable = false)
    private StajGorev stajGorev;

    @Column(name = "madde_metni", nullable = false, length = 2000)
    private String maddeMetni;                    // KOPYA - referans degil

    @Column(name = "sira", nullable = false)
    private Integer sira;

    // OPSIYONEL not: "sunu okudum, sunu denedim". Bos birakilabilir.
    @Column(name = "stajyer_notu", length = 4000)
    private String stajyerNotu;

    // V23: sablonda olmayan, sorumlunun bu staja ozel ekledigi madde.
    // Sablondan kopyalananlarda false kalir.
    @Column(name = "ozel_mi", nullable = false)
    private Boolean ozelMi = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false)
    private GorevDurumu durum = GorevDurumu.BEKLIYOR;

    // ---- Onay alanlari (sorumlu doldurur) ----

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "onaylayan_sorumlu_id")
    private Sorumlu onaylayan;

    @Column(name = "onay_tarihi")
    private LocalDateTime onayTarihi;

    @Column(name = "onay_notu", length = 2000)
    private String onayNotu;
}
