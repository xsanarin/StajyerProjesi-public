package com.example.StajEnvanterProjesi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * ADIM 2 — Sablonun maddesi. Bu satir DEGISEBILIR.
 *
 * Atama sirasinda metni StajGorevMaddesi'ya KOPYALANIR; sonradan burayi degistirmek
 * atanmis gorevleri etkilemez (Gereksinim 3'un snapshot kurali).
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "sablon_maddesi")
@Table(
        name = "sablon_maddesi",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_sablon_maddesi_sira",
                columnNames = {"sablon_id", "sira"}
        )
)
public class SablonMaddesi extends GenericObjectEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "sablon_id", nullable = false)
    private GorevSablonu sablon;

    @Column(name = "madde_metni", nullable = false, length = 2000)
    private String maddeMetni;

    @Column(name = "sira", nullable = false)
    private Integer sira;
}
