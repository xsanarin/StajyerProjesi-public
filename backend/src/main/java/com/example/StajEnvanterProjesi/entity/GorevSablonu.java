package com.example.StajEnvanterProjesi.entity;

import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ADIM 2 — Sablon: departman + seviye ile etiketli "usta kopya".
 *
 * Sablon SILINMEZ, aktif=false yapilir. Silinirse ondan uretilmis gorevlerin
 * izi kopar (staj_gorev.kaynak_sablon_id -> NULL); gorevin kendisi yasar.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "gorev_sablonu")
@Table(name = "gorev_sablonu")
public class GorevSablonu extends GenericObjectEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad", nullable = false)
    private String ad;

    @Column(name = "aciklama", length = 2000)
    private String aciklama;

    // Oneri mekanizmasinin iki ayagindan biri (digeri seviye).
    @ManyToOne(optional = false)
    @JoinColumn(name = "departman_id", nullable = false)
    private Departman departman;

    @Enumerated(EnumType.STRING)
    @Column(name = "seviye", nullable = false)
    private Seviye seviye = Seviye.LEVEL_1;

    // Pasif sablon onerilmez ve atanamaz; gecmis atamalar etkilenmez.
    @Column(name = "aktif", nullable = false)
    private Boolean aktif = true;

    // Cift yonlu iliski: equals/hashCode/toString disi birakilmazsa
    // sablon <-> madde arasinda sonsuz dongu olusur.
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "sablon", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sira ASC")
    private List<SablonMaddesi> maddeler = new ArrayList<>();

    /** Iki yonu birlikte kurar; tek yon set edilirse FK null kalir. */
    public void maddeEkle(SablonMaddesi madde) {
        madde.setSablon(this);
        this.maddeler.add(madde);
    }
}
