package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import com.example.StajEnvanterProjesi.entity.enums.GorevDurumu;
import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ADIM 2 — Staja atanmis gorev.
 *
 * kaynakSablonHashId sablon silinmisse null olur (ON DELETE SET NULL) — gorev yine de
 * eksiksiz doner. ilerleme her yanitin icinde gelir; sorumlu ve stajyer ekranlarinin
 * ikisi de cubugu ayrica istek atmadan cizebilsin diye.
 */
@Data
public class StajGorevResponse {

    private String hashId;
    private String baslik;
    private String aciklama;
    private Seviye seviye;
    private GorevDurumu durum;
    private LocalDateTime atanmaTarihi;
    private String kaynakSablonHashId;
    private IlerlemeResponse ilerleme;
    private List<StajGorevMaddeResponse> maddeler = new ArrayList<>();
}
