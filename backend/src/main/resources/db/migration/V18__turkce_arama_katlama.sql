-- V18 — Turkce harfe duyarsiz arama icin ortak katlama fonksiyonu.
--
-- SORUN: arama uclari "LOWER(alan) LIKE LOWER(:q)" kaliyla yaziliydi, ama bu
-- veritabani C collation ile kurulmus (pg_database.datcollate = 'C'). C
-- ctype'inda LOWER() YALNIZCA ASCII A-Z harflerini kucultur; O, G, S, U, I
-- oldugu gibi kalir. Sonuc: "Sahin" arayan bulur, "sahin" arayan bulamaz.
--   LOWER('GOKTUG') -> 'gOktuG'   (O ve G katlanmadi)
--   LOWER('Goktug') -> 'goktug'
-- Iki taraf esitlenmedigi icin eslesme kaciyordu.
--
-- COZUM: karsilastirmadan once Turkce harfleri ASCII karsiligina cevir, sonra
-- kucult. Boylece arama hem buyuk/kucuk hem sapkali/sapkasiz duyarsiz olur --
-- "goktug" yazan "Goktug" kaydini da bulur, ki klavye duzeni degisken oldugu
-- icin istenen davranis bu.
--
-- NEDEN COLLATE DEGIL: dogru cozum LOWER(x COLLATE "tr-TR-x-icu") olurdu ama
-- JPQL COLLATE ifadesini uretemiyor; sorgulari native'e cevirmek gerekirdi.
-- Fonksiyon JPQL'den FUNCTION('staj_katla', ...) ile cagrilabiliyor.
--
-- NEDEN BURADA: Turkce harfler .sql dosyalarinda duruyor, Java kaynagi ASCII
-- kaliyor (projenin kurali). Ayrica tek tanim -- ucler arasi davranis farki
-- olusamaz.
--
-- translate() from/to ESIT UZUNLUKTA olmali, yoksa fazla karakterler sessizce
-- SILINIR. 12 harf: c g i o s u C G I O S U karsiliklari.
-- 'I' ve 'i' zaten ASCII, listede yoklar; noktali 'İ' -> 'I' sonra LOWER ile
-- 'i' olur, yani "İstanbul"/"istanbul"/"ISTANBUL" ucu de ayni yere duser.
--
-- IMMUTABLE + STRICT: ayni girdi daima ayni cikti (planlayici sabit katlayabilir)
-- ve NULL girdi NULL doner -- LEFT JOIN'de bos yedek sorumlu icin sart.
-- Ileride arama yavaslarsa bu fonksiyon uzerine ifade indeksi kurulabilir:
--   CREATE INDEX ... ON stajyer (staj_katla(ad || ' ' || soyad) text_pattern_ops);

CREATE OR REPLACE FUNCTION public.staj_katla(metin text)
    RETURNS text
    LANGUAGE sql
    IMMUTABLE
    STRICT
    PARALLEL SAFE
AS $$
    SELECT lower(translate(metin, 'çğıöşüÇĞİÖŞÜ', 'cgiosuCGIOSU'));
$$;

COMMENT ON FUNCTION public.staj_katla(text) IS
    'Arama karsilastirmasi icin metni ASCII''ye indirip kucultur (C collation''da LOWER() Turkce harfleri katlamiyor).';
