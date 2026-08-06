package com.example.StajEnvanterProjesi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

/**
 * ADIM 0 — DEPRECATED. Asil/yedek sorumlu iliskisi artik Staj entity'sindeki
 * asil_sorumlu_id / yedek_sorumlu_id kolonlariyla tutuluyor (bkz. Staj, ADIM 3 terfi).
 * Bu tablo/endpoint'ler amac netlesene kadar SILINMEDI, sadece kapatildi.
 */
@Deprecated
@Entity(name = "stajyerApplication")
@Table(name ="stajyer_application")
@Data

public class StajyerApplication extends GenericObjectEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Stajyer_id", nullable = false)
    private Stajyer stajyers;

    @ManyToOne
    @JoinColumn(name = "Sorumlu_id", nullable = true)
    private Sorumlu sorumlus;

//    @ManyToOne
//     @JoinColumn(name = "yedekSorumlu_id")

    @Column(name = "asilMi")
    private Boolean asilMi = true;
}
