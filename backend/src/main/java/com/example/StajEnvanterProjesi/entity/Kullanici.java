package com.example.StajEnvanterProjesi.entity;

import com.example.StajEnvanterProjesi.entity.enums.Rol;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * ADIM 2 — Login/yetki hesabi. Profil (Sorumlu/Stajyer) tablolarindan AYRI tutulur:
 *   - rol=SORUMLU ise sorumlu FK dolu
 *   - rol=STAJYER ise stajyer FK dolu
 *   - rol=ADMIN ise ikisi de bos
 * Boylece kimlik (login) ile is verisi (profil) ayrisir; sorumlu ayrilinca (ADIM 3)
 * profili soft-delete edip login hesabini da kilitleriz (status=false).
 */
@Data
@Entity(name = "kullanici")
@Table(name = "kullanici")
public class Kullanici extends GenericObjectEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // BCrypt hash — duz sifre asla saklanmaz. JSON'a da sizmasin diye @JsonIgnore.
    @JsonIgnore
    @Column(name = "sifre_hash", nullable = false)
    private String sifreHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    // Ilk giriste zorunlu sifre degisimi (provisioning'de gecici sifre uretilir).
    @Column(name = "sifre_degistirmeli", nullable = false)
    private Boolean sifreDegistirmeli = true;

    @ManyToOne
    @JoinColumn(name = "sorumlu_id")
    private Sorumlu sorumlu;   // rol=SORUMLU ise dolu

    @ManyToOne
    @JoinColumn(name = "stajyer_id")
    private Stajyer stajyer;   // rol=STAJYER ise dolu
}