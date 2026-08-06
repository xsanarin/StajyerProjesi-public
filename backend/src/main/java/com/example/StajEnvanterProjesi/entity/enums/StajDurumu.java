package com.example.StajEnvanterProjesi.entity.enums;

/**
 * ADIM 0 — Ortak Temizlik.
 * Staj.durum artik serbest String degil, kontrollu bir enum.
 *   DEVAM_EDIYOR : aktif staj (yazma islemleri serbest)
 *   TAMAMLANDI   : puanlama yapildi, staj kapandi (Gorev 4 -> yazma kilitli)
 *   IPTAL        : yarida kesildi
 * DB'de @Enumerated(EnumType.STRING) ile "DEVAM_EDIYOR" gibi metin olarak tutulur
 * (ordinal degil -> siralama degisse bile veri bozulmaz).
 */
public enum StajDurumu {
    DEVAM_EDIYOR,
    TAMAMLANDI,
    IPTAL
}