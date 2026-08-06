package com.example.StajEnvanterProjesi.entity;

import com.example.StajEnvanterProjesi.entity.enums.GorevDurumu;
import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ADIM 2 — Staja atanmis gorev: sablonun DONDURULMUS kopyasi.
 *
 * baslik ve seviye sablondan kopyalanir, referans tutulmaz. kaynakSablon yalnizca
 * "bu nereden geldi?" izidir; sablon silinirse NULL'a duser (ON DELETE SET NULL)
 * ama gorev ve maddeleri ayakta kalir.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "staj_gorev")
@Table(name = "staj_gorev")
public class StajGorev extends GenericObjectEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "staj_id", nullable = false)
    private Staj staj;

    // Sadece iz - kopya bagimsiz. Sablon silinebilir, bu alan null olur.
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "kaynak_sablon_id")
    private GorevSablonu kaynakSablon;

    @Column(name = "baslik", nullable = false)
    private String baslik;                       // KOPYA (sablon.ad)

    @Column(name = "aciklama", length = 2000)
    private String aciklama;

    @Enumerated(EnumType.STRING)
    @Column(name = "seviye", nullable = false)
    private Seviye seviye;

    // Gorev seviyesinde yalnizca BEKLIYOR / TAMAMLANDI kullanilir.
    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false)
    private GorevDurumu durum = GorevDurumu.BEKLIYOR;

    @Column(name = "atanma_tarihi", nullable = false)
    private LocalDateTime atanmaTarihi;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "stajGorev", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sira ASC")
    private List<StajGorevMaddesi> maddeler = new ArrayList<>();

    public void maddeEkle(StajGorevMaddesi madde) {
        madde.setStajGorev(this);
        this.maddeler.add(madde);
    }
}
