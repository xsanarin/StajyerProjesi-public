package com.example.StajEnvanterProjesi.entity.enums;

/**
 * ADIM 0 — Ortak Temizlik.
 * Staj.baslangicLevel (Integer 1/2) yerine tip guvenli enum.
 * Gorev sablonlari bu seviyeye gore onerilir.
 *
 * SIRA ANLAMLIDIR: seviye yukseltme ordinal() ile karsilastirir
 * (StajGorevServiceImpl.seviyeYukselt) ve yalnizca ileri gider. Yeni seviye
 * SONA eklenmeli, aralara serpistirilmemeli.
 *
 * Veritabaninda EnumType.STRING olarak tutuldugu icin sona deger eklemek
 * mevcut kayitlari etkilemez.
 */
public enum Seviye {
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
    LEVEL_4,
    LEVEL_5
}