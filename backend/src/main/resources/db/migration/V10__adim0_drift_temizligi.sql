-- V10 — ADIM 0 sema borcunun kapatilmasi.
--
-- ADIM 0'da bu alanlar entity'lerden kaldirildi ama veritabani 'ddl-auto: update'
-- ile yonetildigi icin sutunlar DB'de kaldi (update sutun ekler, SILMEZ).
-- Sonuc: 'baslangic_level' NOT NULL oldugu icin staj kaydi hic acilamiyordu
-- (ADIM 1 testlerinde 500 hatasi olarak yakalandi).
--
-- Bu sutunlarin hicbiri artik bir entity tarafindan eslenmiyor; okuyan/yazan kod yok.

-- Seviye alani ADIM 0'da Integer 'baslangicLevel' -> Seviye enum 'seviye' oldu.
ALTER TABLE public.staj DROP COLUMN IF EXISTS baslangic_level;

-- Staj tarihleri ve durum ADIM 0'da Stajyer'den Staj'a tasindi.
-- (Ayni kisi farkli donemlerde staj yaparsa her stajin kendi araligi olmali.)
ALTER TABLE public.stajyer DROP COLUMN IF EXISTS staj_baslangic_tarihi;
ALTER TABLE public.stajyer DROP COLUMN IF EXISTS staj_bitis_tarihi;
ALTER TABLE public.stajyer DROP COLUMN IF EXISTS durum;

-- ADIM 0 checklist: yedek sorumlu opsiyonel olmali.
ALTER TABLE public.staj ALTER COLUMN yedek_sorumlu_id DROP NOT NULL;
