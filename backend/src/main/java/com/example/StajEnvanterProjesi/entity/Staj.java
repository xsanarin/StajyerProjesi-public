package com.example.StajEnvanterProjesi.entity;

import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import jakarta.persistence.*;
import lombok.Data;

import com.example.StajEnvanterProjesi.entity.converter.DevamGunleriConverter;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * MERKEZI TABLO. Plan Gorev 5'in temeli:
 *   "Bir kisinin belirli bir donemdeki staj kaydi."
 * Tum operasyonel veri (devam, gorev, degerlendirme) ileride bu Staj'a (staj_id) baglanacak,
 * dogrudan stajyere degil. Ayni kisi ertesi yil tekrar gelirse YENI bir Staj satiri acilir,
 * eski donem verisi dokunulmadan kalir.
 *
 * DB seviyesinde iki kritik kural (cift guvence):
 *   1) UNIQUE(stajyer_id, donem_id) -> ayni kisi ayni donemde iki kez kaydedilemez
 *   2) CHECK(asil_sorumlu_id <> yedek_sorumlu_id) -> ayni stajda asil ve yedek ayni kisi olamaz
 * Not: CHECK kisitini Hibernate her lehcede uretmez; kesin cozum icin Flyway migration'inda
 *      ALTER TABLE ... ADD CONSTRAINT ile eklemek en saglami (ileride Faz 0 migration adimi).
 */
@Data
@Entity(name = "staj")
@Table(
        name = "staj",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_staj_stajyer_donem",
                columnNames = {"stajyer_id", "donem_id"}
        )
)
public class Staj extends GenericObjectEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Kimin, hangi donemde ---
    @ManyToOne(optional = false)
    @JoinColumn(name = "stajyer_id", nullable = false)
    private Stajyer stajyer;


    @ManyToOne(optional = false)
    @JoinColumn(name = "donem_id", nullable = false)
    private Donem donem;

    // --- Departman (ADIM 1) ---
    // Departman kisiye degil STAJA baglidir: ayni stajyer seneye farkli departmanda staj yapabilir.
    // Eslesme kurali (stajyer/asil/yedek ayni departman) StajServiceImpl.save'de dogrulanir.
    @ManyToOne(optional = false)
    @JoinColumn(name = "departman_id", nullable = false)
    private Departman departman;

    // --- Sorumlular (Gorev 1 & 4) ---
    // asil ZORUNLU. yedek ARTIK NULLABLE (ADIM 0): terfi sonrasi yedek slotu gecici bosalir
    // (asil = eski yedek olur, yedek = null; admin yeni yedek atar - ADIM 3).
    @ManyToOne(optional = false)
    @JoinColumn(name = "asil_sorumlu_id", nullable = false)
    private Sorumlu asilSorumlu;

    @ManyToOne(optional = true)
    @JoinColumn(name = "yedek_sorumlu_id", nullable = true)
    private Sorumlu yedekSorumlu;

    // --- Staj tarihleri (ADIM 0: Stajyer'den buraya tasindi) ---
    // Ayni kisi farkli donemlerde staj yaparsa her stajin kendi tarih araligi olur.
    @Column(name = "staj_baslangic_tarihi")
    private LocalDate stajBaslangicTarihi;

    @Column(name = "staj_bitis_tarihi")
    private LocalDate stajBitisTarihi;

    // --- Staj durumu (ADIM 0: String -> enum) ---
    // Gorev 4: degerlendirme yazilinca TAMAMLANDI olur ve yazma islemleri kilitlenir.
    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false)
    private StajDurumu durum = StajDurumu.DEVAM_EDIYOR;

    // Gorev 3: gorev sablonlarinin baslangic seviyesi (ADIM 0: Integer -> enum).
    @Enumerated(EnumType.STRING)
    @Column(name = "seviye", nullable = false)
    private Seviye seviye = Seviye.LEVEL_1;

    // --- Haftalik devam plani (V13) ---
    // Her stajyer haftada 5 gun gelmiyor; kimi 3 gun, kimi 2 gun calisiyor.
    // Bu alan olmadan takvimde bos bir gun "gelmesi gerekmiyordu" mu yoksa
    // "gelmedi" mi ayirt edilemiyor ve devamsizlik hesaplanamiyor.
    // Varsayilan tam mesai; farkli calisanlar icin staj acilirken belirtilir.
    @Convert(converter = DevamGunleriConverter.class)
    @Column(name = "devam_gunleri", nullable = false)
    private Set<DayOfWeek> devamGunleri = new LinkedHashSet<>(List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
}
