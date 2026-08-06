-- V15 — Devam kaydinda cift beyan: stajyer bildirir, sorumlu onaylar.
--
-- Bugune kadar devam kaydini YALNIZCA sorumlu olusturabiliyordu; stajyerin ucu salt
-- okunurdu. Yani "geldi" bilgisi tek bir kisinin beyaniydi ve hicbir sey dogrulamiyordu:
-- sorumlu tiklamayi unutursa gelen stajyer devamsiz gorunuyor, hic gormeden tiklarsa
-- gelmeyen stajyer gelmis sayiliyordu.
--
-- Cozum: stajyer kendi girisini/cikisini bildirir (onaylandi=false), sorumlu onaylar
-- (onaylandi=true). Onaylanmamis gun devam yuzdesine SAYILMAZ.
--
-- onay_kullanici_id: onayi kimin verdigi. Asil ve yedek sorumlu ayni staja
-- onay verebildigi icin "sorumlu" degil "kullanici" tutuluyor (admin de duzeltebilir).

ALTER TABLE public.devam
    ADD COLUMN onaylandi boolean,
    ADD COLUMN onay_tarihi timestamp without time zone,
    ADD COLUMN onay_kullanici_id bigint;

-- Mevcut kayitlarin tamami sorumlu/admin eliyle girilmis, yani zaten onaylidir.
-- Bunlari "onay bekliyor" saymak gecmis devam yuzdelerini geriye donuk bozardi.
UPDATE public.devam
   SET onaylandi = true
 WHERE onaylandi IS NULL;

ALTER TABLE public.devam
    ALTER COLUMN onaylandi SET NOT NULL,
    ALTER COLUMN onaylandi SET DEFAULT true;

ALTER TABLE public.devam
    ADD CONSTRAINT fk_devam_onay_kullanici
        FOREIGN KEY (onay_kullanici_id) REFERENCES public.kullanici (id);

-- Onaylanmamis kaydin onay tarihi/onaylayani olamaz.
ALTER TABLE public.devam
    ADD CONSTRAINT chk_devam_onay_tutarli
        CHECK (onaylandi = true OR (onay_tarihi IS NULL AND onay_kullanici_id IS NULL));
