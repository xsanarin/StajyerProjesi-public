-- V17 — Seviye LEVEL_5'e kadar genisletildi.
--
-- Seviye enum'u LEVEL_1/LEVEL_2 ile sinirliydi; is tarafi 5 kademe istiyor.
-- Java enum'una deger eklemek TEK BASINA YETMEZ: seviye kolonlari varchar +
-- CHECK kisiti ile tutuluyor ve kisit yalnizca iki degere izin veriyordu.
-- Eklemeden once LEVEL_3 ile staj acmak "violates check constraint
-- staj_seviye_check" ile 500 donuyordu.
--
-- ddl-auto: validate oldugu icin semayi Hibernate degil bu dosya degistirir.
-- Kisitlar DROP + ADD ile yeniden kuruluyor; ALTER CONSTRAINT ile CHECK
-- ifadesi degistirilemiyor.
--
-- Sira ANLAMLI: Seviye.ordinal() yukseltmede karsilastiriliyor, yeni degerler
-- listenin SONUNA eklendi. Mevcut kayitlar LEVEL_1/LEVEL_2 oldugu icin
-- genisleyen kisit hicbir satiri gecersiz kilmaz, geri veri donusumu gerekmez.

ALTER TABLE public.staj
    DROP CONSTRAINT IF EXISTS staj_seviye_check;
ALTER TABLE public.staj
    ADD CONSTRAINT staj_seviye_check
        CHECK (((seviye)::text = ANY ((ARRAY[
            'LEVEL_1'::character varying,
            'LEVEL_2'::character varying,
            'LEVEL_3'::character varying,
            'LEVEL_4'::character varying,
            'LEVEL_5'::character varying])::text[])));

ALTER TABLE public.gorev_sablonu
    DROP CONSTRAINT IF EXISTS gorev_sablonu_seviye_check;
ALTER TABLE public.gorev_sablonu
    ADD CONSTRAINT gorev_sablonu_seviye_check
        CHECK (((seviye)::text = ANY ((ARRAY[
            'LEVEL_1'::character varying,
            'LEVEL_2'::character varying,
            'LEVEL_3'::character varying,
            'LEVEL_4'::character varying,
            'LEVEL_5'::character varying])::text[])));

-- Staja islenmis gorevin seviyesi de ayni kisiti tasiyor: LEVEL_3 sablonu
-- atanabilsin diye burasi da genisliyor.
ALTER TABLE public.staj_gorev
    DROP CONSTRAINT IF EXISTS staj_gorev_seviye_check;
ALTER TABLE public.staj_gorev
    ADD CONSTRAINT staj_gorev_seviye_check
        CHECK (((seviye)::text = ANY ((ARRAY[
            'LEVEL_1'::character varying,
            'LEVEL_2'::character varying,
            'LEVEL_3'::character varying,
            'LEVEL_4'::character varying,
            'LEVEL_5'::character varying])::text[])));