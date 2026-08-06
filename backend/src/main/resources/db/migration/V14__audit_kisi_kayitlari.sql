-- V14 — Denetim (audit): kisi kayitlarini KIM olusturdu / KIM (soft) sildi.
--
-- Ihtiyac: bir stajyer/sorumlu kaydini hangi giris yapmis admin ekledi, hangisi sildi
-- gorulebilsin. Soft delete ile satir DB'de kaldigi icin bu bilgiyi kaydin uzerinde
-- tutmak yeterli; ayri bir audit tablosuna gerek duyulmadi (yalnizca create/delete izi,
-- tam degisiklik gecmisi degil).
--
-- Kolonlar giris yapan kullanicinin id'sini (kullanici.id) tutar. Bilerek FOREIGN KEY
-- KONULMADI: audit izi kullanici yasam dongusunden bagimsiz kalmali (kullanici kaydi
-- ileride ne olursa olsun "kim sildi" bilgisi bozulmasin). HTTP disi akislarda
-- (seeder/zamanlanmis is) giris yapan kullanici olmadigindan NULL kalabilir.

ALTER TABLE public.stajyer
    ADD COLUMN olusturan_kullanici_id bigint,
    ADD COLUMN silen_kullanici_id     bigint,
    ADD COLUMN silme_tarihi           timestamp without time zone;

ALTER TABLE public.sorumlu
    ADD COLUMN olusturan_kullanici_id bigint,
    ADD COLUMN silen_kullanici_id     bigint,
    ADD COLUMN silme_tarihi           timestamp without time zone;
