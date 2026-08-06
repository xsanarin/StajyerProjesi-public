package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import lombok.Data;

/**
 * ADIM 2 — Gorev ilerlemesi. Frontend "7/12 tamamlandi · %58" cubugu icin.
 *
 * tamamlanan = TAMAMLANDI + ONAYLANDI. REDDEDILDI sayilmaz: stajyerin
 * yeniden yapmasi gereken bir madde "bitmis" degildir.
 */
@Data
public class IlerlemeResponse {

    private int toplam;
    private int tamamlanan;
    private int yuzde;
}
