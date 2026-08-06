package com.example.StajEnvanterProjesi.entity.dto.Devam;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Aylik takvim satiri — artik AYIN HER GUNU icin bir satir doner (kaydi olmayan
 * gunler dahil), boylece frontend takvimi eksiksiz cizebilir.
 *
 * durum (serviste hesaplanir):
 *   TAM           giris + cikis var, sorumlu onaylamis
 *   CIKIS_YOK     giris var, cikis yok, sorumlu onaylamis
 *   ONAY_BEKLIYOR stajyer bildirmis ama sorumlu henuz onaylamamis (V15)
 *   GELMEDI       PLANLI gundu, gecmiste kaldi, kayit yok  -> devamsizlik
 *   BEKLENIYOR    planli ama gun henuz gelmedi/bugun
 *   PLANSIZ       o gun gelmesi beklenmiyor (hafta tatili ya da plan disi)
 *
 * planli: gun stajin haftalik devam planinda mi? planli=false + kayit var ise
 * stajyer plan disi (telafi) gelmis demektir.
 */
@Data
public class DevamResponse {

    // DISARIYA sadece hashId (UUID) — Long id disari sizmaz (GenericObjectEntity ADIM 0 kurali).
    // Admin bu hashId ile ilgili kaydi silebilir/duzeltebilir.
    private String hashId;
    private LocalDate tarih;
    private LocalDateTime girisSaati;
    private LocalDateTime cikisSaati;
    private Boolean manuelMi;
    private String durum;

    // V15 — sorumlu onayladi mi? false ise gun devam yuzdesine sayilmaz.
    private Boolean onaylandi;

    // Bu gun stajin haftalik planinda mi? Takvimde plan disi gunler soluk cizilir.
    private Boolean planli;
}
