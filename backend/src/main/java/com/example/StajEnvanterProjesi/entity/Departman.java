package com.example.StajEnvanterProjesi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * ADIM 1 — Departman (Java, .NET, Database ...).
 * Mevcut CRUD kalibi (Donem modulu) ornek alinarak yazildi.
 *
 * KRITIK KARAR: Departman kisiye degil STAJA baglidir. Ayni stajyer seneye farkli
 * departmanda staj yapabilir. Bu yuzden Departman FK'si Staj'a ve Sorumlu'ya eklendi,
 * Stajyer'e EKLENMEDI.
 */
@Data
@Entity(name = "departman")
@Table(name = "departman")
public class Departman extends GenericObjectEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad", nullable = false, unique = true)
    private String ad;   // Java, .NET, Database...
}