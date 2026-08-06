-- V22 — BT talebine stajyer atamasi.
--
-- V21'de talep yalnizca DEPARTMANA baglaniyordu; "bu talep kimin icin acildi"
-- sorusunun cevabi aciklama metnine yaziliyordu. Artik ayri bir kolon: talep
-- bir stajyere atanabilir ve atanan kisi hem admin hem sorumlu listesinde
-- kolon olarak gorunur.
--
-- NULLABLE, bilerek: departman geneline acilan talepler (ornegin "toplanti
-- odasina projeksiyon") kimseye ait degildir. Zorunlu yapmak, mevcut iki
-- kaydin ve bu tur taleplerin yerini bozardi.
--
-- Kolon stajyer_id, staj_id DEGIL: talep "kisi" hakkindadir (bu stajyere laptop
-- kurulacak), belirli bir donemin staj kaydi hakkinda degil. Stajyerin hangi
-- departmanda oldugu ise staj uzerinden cozulur — bu yuzden servis, atanan
-- stajyerin secilen departmanda AKTIF staji oldugunu dogrular; veritabani bunu
-- tek basina garanti edemez (iki tablo arasi kural).

ALTER TABLE public.talep
    ADD COLUMN stajyer_id bigint;

ALTER TABLE public.talep
    ADD CONSTRAINT talep_stajyer_fk
        FOREIGN KEY (stajyer_id) REFERENCES public.stajyer(id);

-- "Bu stajyere acilmis talepler" sorgusu icin. Kismi index: atanmamis talepler
-- (stajyer_id NULL) ve silinmis kayitlar indexte yer kaplamaz.
CREATE INDEX idx_talep_stajyer_aktif
    ON public.talep (stajyer_id)
 WHERE stajyer_id IS NOT NULL AND status = true;
