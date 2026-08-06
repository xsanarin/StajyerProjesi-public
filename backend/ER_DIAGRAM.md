# Staj Envanter Projesi - ER Diyagramı

> Not: `StajyerApplication` tablosu kaldırılacağı için bu diyagrama dahil edilmemiştir.
> Devir sırasında yeni yedek sorumlu seçimi admin tarafından manuel yapılır (şema değişikliği gerektirmez).
> Değerlendirme kaydı girildiğinde `Staj.durum = TAMAMLANDI` olur ve `Devam` / `StajGorevMaddesi` üzerine yeni yazma işlemleri kilitlenir (iş kuralı, şema değişikliği değil).
> İlerleme çubuğu `StajGorevMaddesi.durum` alanına göre hesaplanır: bir `StajGorev` için tamamlanan madde sayısı / toplam madde sayısı (`TAMAMLANDI` + `ONAYLANDI` bitmiş sayılır, `REDDEDILDI` sayılmaz).
> **Madde bir soru değil, yapılacak iştir** ("JVM nedir, ne işe yarar"). Stajyer işi yapıp tamamlandı işaretler, sorumlu onaylar; `stajyer_notu` opsiyoneldir.
> `staj_gorev_madde.madde_metni` bir FK **değil**, `sablon_maddesi`'nden alınan bağımsız metin kopyasıdır (snapshot) — şablon değişse de atanmış görevler değişmez.

```mermaid
erDiagram
    STAJYER ||--o{ STAJ : "stajyer_id"
    DONEM ||--o{ STAJ : "donem_id"
    SORUMLU ||--o{ STAJ : "asil_sorumlu_id"
    SORUMLU ||--o{ STAJ : "yedek_sorumlu_id"

    STAJ ||--o{ DEVAM : "gunluk giris-cikis"
    STAJ ||--o{ STAJ_GOREV : "atanan gorevler"
    GOREV_SABLONU ||--o{ SABLON_MADDESI : icerir
    GOREV_SABLONU |o..o{ STAJ_GOREV : "kaynak sablon (opsiyonel, sadece iz)"
    STAJ_GOREV ||--o{ STAJ_GOREV_MADDE : icerir
    STAJ ||--o| DEGERLENDIRME : "1 staj = 1 puan"
    SORUMLU ||--o{ DEGERLENDIRME : degerlendirir

    STAJYER {
        bigint id PK
        string ad
        string soyad
        string email
        string telefon
        string universite
        string bolum
        int sinif
        date staj_baslangic_tarihi
        date staj_bitis_tarihi
        string durum
        boolean status
    }

    SORUMLU {
        bigint id PK
        string ad
        string soyad
        string email
        string telefon
    }

    DONEM {
        bigint id PK
        string ad
        date baslangic_tarihi
        date bitis_tarihi
        boolean aktif
    }

    STAJ {
        bigint id PK
        bigint stajyer_id FK
        bigint donem_id FK
        bigint asil_sorumlu_id FK
        bigint yedek_sorumlu_id FK
        string durum
        int baslangic_level
    }

    DEVAM {
        bigint id PK
        bigint staj_id FK
        date tarih
        datetime giris_saati
        datetime cikis_saati
    }

    GOREV_SABLONU {
        bigint id PK
        string ad
        string aciklama
        bigint departman_id FK
        string seviye
        boolean aktif
    }

    SABLON_MADDESI {
        bigint id PK
        bigint sablon_id FK
        string madde_metni
        int sira
    }

    STAJ_GOREV {
        bigint id PK
        bigint staj_id FK
        bigint kaynak_sablon_id FK
        string baslik
        string aciklama
        string seviye
        string durum
        datetime atanma_tarihi
    }

    STAJ_GOREV_MADDE {
        bigint id PK
        bigint staj_gorev_id FK
        string madde_metni
        int sira
        string stajyer_notu
        boolean ozel_mi
        string durum
        bigint onaylayan_sorumlu_id FK
        datetime onay_tarihi
        string onay_notu
    }

    DEGERLENDIRME {
        bigint id PK
        bigint staj_id FK
        bigint degerlendiren_sorumlu_id FK
        int puan
        string yorum
        datetime tarih
    }
```
