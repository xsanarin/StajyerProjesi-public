-- TEK ASIL KURALI
-- Bir sorumlu ayni anda YALNIZCA BIR devam eden stajin asil sorumlusu olabilir.
-- Yedeklik kisitlanmaz: ayni kisi istedigi kadar stajin yedegi olabilir.
--
-- Kismi (partial) unique index kullaniliyor cunku kural sadece "acik" stajlar icin
-- gecerli:
--   durum = 'DEVAM_EDIYOR' -> staj TAMAMLANDI olunca sorumlu kendiliginden bosa duser,
--                             ayrica bir "serbest birak" islemi gerekmez.
--   status = true          -> soft-delete edilen staj (status=false) durumu
--                             DEVAM_EDIYOR olarak kaldigi icin, suzulmezse silinmis bir
--                             kayit sorumluyu sonsuza kadar mesgul gosterirdi.
--
-- Servis katmaninda ayni kural StajServiceImpl.asilMusaitMi() ile de uygulanir; bu index
-- son savunma hatti (dogrudan SQL ya da atlanan bir kod yolu icin).
--
-- NOT: Bu index mevcut veride ihlal varsa OLUSMAZ ve migration basarisiz olur, bu da
-- uygulamanin acilmasini engeller. Kurmadan once su sorgu bos donmeli:
--   SELECT asil_sorumlu_id, count(*) FROM staj
--    WHERE durum = 'DEVAM_EDIYOR' AND status = true
--    GROUP BY asil_sorumlu_id HAVING count(*) > 1;

CREATE UNIQUE INDEX uk_staj_asil_sorumlu_devam
    ON public.staj (asil_sorumlu_id)
    WHERE durum = 'DEVAM_EDIYOR' AND status = true;
