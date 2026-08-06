package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Gorev.IlerlemeResponse;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.OzelMaddeRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonResponse;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.MaddeTamamlaRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.MaddeOnayRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.StajGorevResponse;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.StajGorevMaddeResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajResponse;
import com.example.StajEnvanterProjesi.entity.enums.Seviye;

import java.util.List;

/**
 * ADIM 2 (KISI-B) — Sablon/Gorev sistemi.
 *
 * SNAPSHOT: sablon ataninca madde METINLERI kopyalanir. Sablon sonradan degisse de
 * atanmis gorevler degismez (Gereksinim 3). Yazma islemleri ADIM 1'in ortak kapisi
 * benimAktifStajim'dan gecer; okuma islemleri sahiplik arar ama tamamlanmis staja
 * da izin verir (gecmis salt-okunur gorunur).
 */
public interface StajGorevService {

    // ---- Sorumlu tarafi ----

    /** Stajin departman + seviyesine uyan aktif sablonlar. */
    List<SablonResponse> onerilenSablonlar(String stajHashId, Long sorumluId);

    /** Sistemin kalbi: sablonun DONDURULMUS kopyasini staja atar. */
    StajGorevResponse sablonAta(String stajHashId, String sablonHashId, Long sorumluId);

    List<StajGorevResponse> stajGorevleri(String stajHashId, Long sorumluId);

    IlerlemeResponse ilerleme(String gorevHashId, Long sorumluId);

    /** Stajyerin tamamladigi maddeyi ONAYLANDI / REDDEDILDI yapar. */
    StajGorevMaddeResponse maddeOnayla(String maddeHashId, MaddeOnayRequest request, Long sorumluId);

    /**
     * V23 — Sablonda olmayan, sorumlunun bu staja ozel ekledigi madde (ozelMi=true).
     * Sablonun kendisine dokunmaz: ekleme yalnizca bu stajin snapshot'ina yazilir.
     */
    StajGorevResponse ozelMaddeEkle(String gorevHashId, OzelMaddeRequest request, Long sorumluId);

    /**
     * V23 — Yanlis eklenen ozel maddeyi siler. YALNIZCA ozelMi=true maddeler:
     * sablondan gelen madde snapshot'in parcasidir, cikarilamaz. Onaylanmis madde
     * de silinemez (is kaydi). Silme sonrasi gorev durumu yeniden hesaplanir.
     */
    StajGorevResponse ozelMaddeSil(String maddeHashId, Long sorumluId);

    /** Yalnizca ileri yonde; mevcut seviyenin gorevleri bitmeden yukseltilemez. */
    StajResponse seviyeYukselt(String stajHashId, Seviye yeniSeviye, Long sorumluId);

    // ---- Stajyer tarafi ----

    /** Gecmis donemler dahil stajyerin tum stajlari. */
    List<StajResponse> stajlarim(Long stajyerId);

    List<StajGorevResponse> gorevlerim(String stajHashId, Long stajyerId);

    /** Kendi maddesi + staj aktif olmali; tamamlanmis staj salt-okunur. */
    StajGorevMaddeResponse maddeTamamla(String maddeHashId, MaddeTamamlaRequest request, Long stajyerId);
}
