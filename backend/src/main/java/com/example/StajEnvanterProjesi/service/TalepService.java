package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerResponse;
import com.example.StajEnvanterProjesi.entity.dto.Talep.TalepRequest;
import com.example.StajEnvanterProjesi.entity.dto.Talep.TalepResponse;

import java.util.List;

/**
 * BT talep is kurallarinin sozlesmesi.
 *
 * Yetki ayrimi metot isimlerinde gorunur: olustur/guncelle/sil yalnizca ADMIN
 * ucundan cagrilir, sorumluListele yalnizca SORUMLU ucundan. Ayrim ayrica
 * SecurityConfig'te yol onekiyle de saglanir (/api/admin/** ve /api/sorumlu/**).
 */
public interface TalepService {

    TalepResponse olustur(TalepRequest request);

    TalepResponse guncelle(String hashId, TalepRequest request);

    TalepResponse getir(String hashId);

    /** Admin: tum aktif talepler. */
    List<TalepResponse> listele();

    /** Sorumlu: yalnizca kendi departmaninin talepleri. */
    List<TalepResponse> sorumluListele(Long sorumluId);

    /**
     * V22 — Talep dialogundaki kisi seciminin kaynagi: secilen departmanda staj
     * yapan stajyerler. Departman degisince liste de degistigi icin ayri uc.
     */
    List<StajyerResponse> departmanStajyerleri(String departmanHashId);

    /** Soft delete (status=false); kayit veritabaninda kalir. */
    void sil(String hashId);
}
