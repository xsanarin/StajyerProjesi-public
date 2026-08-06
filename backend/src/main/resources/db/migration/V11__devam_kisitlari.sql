-- V11 — ADIM 1 devam kisitlarinin DB seviyesine tasinmasi.
--
-- UNIQUE(staj_id, tarih) Hibernate tarafindan zaten olusturulmustu (@UniqueConstraint).
-- Eksik olan CHECK: girissiz cikis su an yalnizca serviste engelleniyor; dogrudan
-- SQL ile ya da ileride yazilacak baska bir servis yolundan bu kural delinebilir.
-- ROADMAP:202 bunu DB seviyesinde istiyor.

ALTER TABLE public.devam
    ADD CONSTRAINT chk_devam_giris
    CHECK (cikis_saati IS NULL OR giris_saati IS NOT NULL);
