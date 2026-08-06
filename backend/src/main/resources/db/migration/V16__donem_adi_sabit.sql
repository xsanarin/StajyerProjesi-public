-- V16 — Donem adi sabit kume: Yaz / Güz / Bahar.
--
-- Ad serbest metindi ve veritabani test artiklariyla dolmustu ("D-9675",
-- "BDonem-30225", "2026 Yaz 1784186017"...). Staj donemi kavrami aslinda ucludur;
-- serbest metin hem tekrar hem yazim hatasi uretiyordu.
--
-- Kisit YALNIZCA aktif kayitlara uygulanir (status = true): gecmiste acilmis ve
-- soft-delete edilmis eski donemler oldugu gibi kalir, onlara bagli stajlarin
-- donem adi bozulmaz. Yeni/aktif her donem uc addan biri olmak zorunda.

ALTER TABLE public.donem
    ADD CONSTRAINT chk_donem_ad_sabit
        CHECK (status = false OR ad IN ('Yaz', 'Güz', 'Bahar'));

-- Ayni ad iki kez aktif olamaz: uc ad = en fazla uc donem.
-- Kismi unique index: silinmis kayitlar sayilmaz, ad yeniden kullanilabilir.
CREATE UNIQUE INDEX uq_donem_ad_aktif
    ON public.donem (ad)
 WHERE status = true;
