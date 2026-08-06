package com.example.StajEnvanterProjesi.entity.enums;

/**
 * ADIM 2 — Gorev ve madde yasam dongusu.
 *
 * Akis: BEKLIYOR -> (stajyer yapti isaretler) TAMAMLANDI -> (sorumlu bakar) ONAYLANDI | REDDEDILDI
 *
 * NOT: KISI-B.md "3 durumlu enum" diyor ama ayni dosyada sorumlunun maddeyi
 * ONAYLANDI/REDDEDILDI yapmasi isteniyor; ikisi birlikte 4 durum eder.
 * REDDEDILDI ayri bir durum olarak tutuldu, cunku stajyerin yaptigi isin geri
 * cevrildigini gorebilmesi gerekiyor — BEKLIYOR'a dondurulseydi bu bilgi kaybolurdu.
 *
 * StajGorev seviyesinde yalnizca BEKLIYOR ve TAMAMLANDI kullanilir:
 * gorevin tum maddeleri bitince gorev TAMAMLANDI olur.
 */
public enum GorevDurumu {
    BEKLIYOR,
    TAMAMLANDI,
    ONAYLANDI,
    REDDEDILDI
}
