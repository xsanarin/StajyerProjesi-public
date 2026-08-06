-- V23 — Ozel madde ozelligi geri getirildi (V19'da kaldirilmisti).
--
-- Gerekce: sablon her stajyere birebir uymuyor; sorumlu o staja ozgu bir is
-- eklemek istiyor (ornegin "bu projede Redis kurulumu da yapilacak"). Sablonu
-- degistirmek yanlis cozum: sablon ORTAK, degisiklik sonraki tum stajyerlere
-- da yansirdi. Bu yuzden madde stajin kendi snapshot'ina ekleniyor.
--
-- ozel_mi kolonu geri geliyor: "bu madde sablondan gelmedi, sorumlu ekledi"
-- bilgisi arayuzde rozetle gosteriliyor ve sablon ilerlemesi ile karisimiyor.
--
-- DEFAULT false: V19 oncesinden kalan satirlarin hepsi sablondan geldi
-- (V19 ozel isaretini kaybettirmisti, satirlari silmemisti). Yeni kolonu
-- false ile doldurmak dogru varsayim.
--
-- NOT NULL + DEFAULT birlikte: entity alani primitif olmayan Boolean ama
-- nullable=false; kolonun default'u olmadan mevcut satirlar NULL kalirdi ve
-- ddl-auto=validate ayaga kalkarken degil, ilk okumada patlardi.

ALTER TABLE public.staj_gorev_madde ADD COLUMN ozel_mi boolean NOT NULL DEFAULT false;
