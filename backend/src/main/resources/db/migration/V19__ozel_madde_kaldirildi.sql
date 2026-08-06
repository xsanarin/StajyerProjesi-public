-- V19 — Ozel madde ozelligi kaldirildi, ozel_mi kolonu dusuruldu.
--
-- Sorumlunun staja sonradan madde eklemesi (POST /api/sorumlu/gorevler/{id}/madde)
-- kaldirildi; madde listesi artik yalnizca sablon atamasindan gelen SNAPSHOT'tan
-- olusuyor. Kolonun tek anlami "bu madde sablondan gelmedi" idi -- ekleme ucu
-- olmayinca her madde sablondan geliyor, deger daima false olurdu.
--
-- DIKKAT: geri donusu yok. Halihazirda ozel_mi = true olan satirlar SILINMIYOR,
-- yalnizca ozel olduklari bilgisi kayboluyor; maddeler ve durumlari duruyor.
--
-- Entity alani (StajGorevMaddesi.ozelMi) ayni commit'te kaldirildi. ddl-auto
-- validate oldugu icin ikisi birlikte gitmek zorunda: kolon kalirsa entity'de
-- karsiligi olmadigi icin sorun olmaz ama alan kalir kolon giderse uygulama
-- ayaga kalkmaz.

ALTER TABLE public.staj_gorev_madde DROP COLUMN ozel_mi;
