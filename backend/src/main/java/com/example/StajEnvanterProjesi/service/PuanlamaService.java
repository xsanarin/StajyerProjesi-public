package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.DegerlendirmeResponse;
import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.PuanDuzeltRequest;
import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.PuanRequest;

/**
 * ADIM 4 — Puanlama ve staj kapanisi sozlesmesi.
 */
public interface PuanlamaService {

    /** Asil sorumlu DEVAM_EDIYOR stajini puanlar; staj TAMAMLANDI olur. */
    DegerlendirmeResponse puanla(String stajHashId, Long sorumluId, PuanRequest request);

    /**
     * FRONTEND ADIMI — Stajin mevcut degerlendirmesini okur.
     *
     * NEDEN: puan disariya yalnizca puanla/duzelt/kilidi-ac YANITLARINDA cikiyordu ve
     * StajResponse'ta puan alani yok. Admin duzeltme ekrani bu yuzden mevcut puani
     * gosteremiyor, istemci korlemesine yeni deger yaziyordu.
     *
     * Henuz puanlanmamis stajda HATA ATMAZ: puan'i null olan bir yanit doner. Boylece
     * cagiran taraf "staj yok" (hata) ile "puan yok" (bos alan) durumlarini ayirir.
     */
    DegerlendirmeResponse degerlendirmeGetir(String stajHashId);

    /**
     * FRONTEND ADIMI — Sorumlunun kendi stajinin degerlendirmesi.
     *
     * degerlendirmeGetir ile ayni ciktiyi verir ama SAHIPLIK KAPISINDAN gecer: yalnizca
     * stajin asil ya da yedek sorumlusu okuyabilir (admin ucu herkesi okur, o /api/admin
     * altinda kilitli). Puanlama yetkisi asil sorumluda olsa da OKUMA yedege de acik —
     * yedek terfi ettiginde gecmisi gormesi gerekir.
     *
     * Aktiflik ARANMAZ: puanlanmis (TAMAMLANDI) stajin puani zaten asil okunmak istenen sey.
     */
    DegerlendirmeResponse degerlendirmeGetirSorumlu(String stajHashId, Long sorumluId);

    /** Admin mevcut puani gerekce ile duzeltir. */
    DegerlendirmeResponse adminPuanDuzelt(String stajHashId, PuanDuzeltRequest request);

    /** Admin uc durumda stajin yazma kilidini acar (TAMAMLANDI -> DEVAM_EDIYOR). */
    DegerlendirmeResponse adminKilidiAc(String stajHashId);
}
