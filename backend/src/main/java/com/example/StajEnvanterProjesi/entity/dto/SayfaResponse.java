package com.example.StajEnvanterProjesi.entity.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

/**
 * Sayfali liste yaniti (tum listeleme uclari icin ortak zarf).
 *
 * Spring'in Page nesnesini dogrudan JSON'a yazmiyoruz: govdesi (pageable, sort,
 * first/last/numberOfElements...) surumden surume degisiyor ve Spring Boot bunu
 * "kararsiz sozlesme" diye uyariyor. Disariya kendi sabit alanlarimizi veriyoruz;
 * istemci sadece bu bes alani bilir.
 */
@Data
public class SayfaResponse<T> {

    private List<T> icerik;

    /** 0'dan baslar. */
    private int sayfa;

    /** Istenen sayfa boyutu. */
    private int boyut;

    /** Filtreye uyan TOPLAM kayit sayisi (sadece bu sayfadaki degil). */
    private long toplamKayit;

    private int toplamSayfa;

    /**
     * Page<Entity> -> SayfaResponse<Dto>. Cevirici her serviste zaten var,
     * disaridan gecirilir ki bu sinif entity'lerden bagimsiz kalsin.
     */
    /**
     * Sayfa boyutunun ust siniri. Olmazsa istemci ?boyut=100000 diyip tum tabloyu
     * ceker ve sayfalamayi yapmis olmayiz.
     */
    public static final int EN_BUYUK_BOYUT = 100;

    /** Istemciden gelen sayfa/boyut degerlerini guvenli araliga cekip Pageable uretir. */
    public static Pageable istek(int sayfa, int boyut, Sort sirala) {
        int guvenliSayfa = Math.max(sayfa, 0);
        int guvenliBoyut = Math.min(Math.max(boyut, 1), EN_BUYUK_BOYUT);
        return PageRequest.of(guvenliSayfa, guvenliBoyut, sirala);
    }

    /**
     * Arama terimini LIKE kalibina sarar. Bos terimde "%%" doner, yani "hepsi".
     *
     * NEDEN NULL DEGIL: parametreyi null gecince Postgres tipini cikaramiyor ve
     * sorgu "function lower(bytea) does not exist" ile patliyor. Her zaman metin
     * gonderiyoruz; "%%" kalibi ad/soyad gibi ZORUNLU alanlarda daima eslestigi
     * icin bos arama tum kayitlari dondurur.
     *
     * Terim bagli parametre olarak gidiyor, SQL enjeksiyonu yok. Kullanicinin
     * yazdigi % veya _ joker gibi davranir — sonucu yalnizca genisletebilecegi
     * icin ayrica ESCAPE ile ugrasmiyoruz.
     */
    public static String benzerlik(String q) {
        if (q == null || q.isBlank()) {
            return "%%";
        }
        return "%" + q.trim() + "%";
    }

    public static <E, T> SayfaResponse<T> of(Page<E> page, Function<E, T> cevirici) {
        SayfaResponse<T> r = new SayfaResponse<>();
        r.setIcerik(page.getContent().stream().map(cevirici).toList());
        r.setSayfa(page.getNumber());
        r.setBoyut(page.getSize());
        r.setToplamKayit(page.getTotalElements());
        r.setToplamSayfa(page.getTotalPages());
        return r;
    }
}