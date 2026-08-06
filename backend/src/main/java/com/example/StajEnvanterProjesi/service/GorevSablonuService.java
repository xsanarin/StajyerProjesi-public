package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonResponse;

import java.util.List;

/**
 * ADIM 2 (KISI-B) — Sablon yonetimi (admin).
 *
 * Sablon "usta kopya"dir: degistirilebilir, ama atanmis gorevleri ETKILEMEZ
 * (snapshot kurali — StajGorevService.sablonAta'ya bakiniz).
 */
public interface GorevSablonuService {

    SablonResponse olustur(SablonRequest request);

    /** Maddeler tumuyle yenisiyle degistirilir; atanmis gorevler etkilenmez. */
    SablonResponse guncelle(String hashId, SablonRequest request);

    /**
     * SILME YOK — aktif=false. Pasif sablon onerilmez ve atanamaz, ama gecmis
     * atamalarin izi (staj_gorev.kaynak_sablon_id) korunur.
     */
    void pasiflestir(String hashId);

    SablonResponse getir(String hashId);

    /**
     * departmanHashId verilirse yalnizca o departmanin sablonlari doner; verilmezse
     * tumu. Admin ekrani her zaman bir departman secili calisir.
     */
    List<SablonResponse> listele(boolean sadeceAktif, String departmanHashId);
}
