-- V13 — Stajin haftalik devam gunleri.
--
-- Her stajyer haftada 5 gun gelmiyor: kimi 3 gun, kimi 2 gun. Bugune kadar sistem
-- yalnizca NE OLDUGUNU kaydediyordu, NE OLMASI GEREKTIGINI bilmiyordu. Sonucu:
--   - Takvimde bos bir gun "gelmesi gerekmiyordu" mu "gelmedi" mi ayirt edilemiyordu.
--   - Devamsizlik sayisi hesaplanamiyordu (degerlendirmede ilk sorulacak sey).
--
-- Tek kolon, virgulle ayrilmis java.time.DayOfWeek adlari: "MONDAY,WEDNESDAY,FRIDAY".
-- Ayri tablo acilmadi: bu veri her zaman stajla birlikte okunuyor, tek basina
-- sorgulanmiyor. Plan staj suresince degisirse ileride tarih araliklari ile ayri tabloya
-- tasinabilir (simdilik ihtiyac yok).

ALTER TABLE public.staj
    ADD COLUMN devam_gunleri character varying(255);

-- Mevcut kayitlar: varsayilan tam mesai (Pzt-Cum). Boylece eski stajlarin davranisi
-- degismez; yalnizca farkli calisan stajyerler icin ozel deger girilir.
UPDATE public.staj
   SET devam_gunleri = 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY'
 WHERE devam_gunleri IS NULL;

ALTER TABLE public.staj
    ALTER COLUMN devam_gunleri SET NOT NULL;

-- Bos plan anlamsiz: hicbir gun gelmeyen bir staj olamaz.
ALTER TABLE public.staj
    ADD CONSTRAINT chk_staj_devam_gunleri CHECK (length(btrim(devam_gunleri)) > 0);
