# Postman Koleksiyonları

Tek koleksiyon var. Kendi verisini baştan kurar; gerekli değişkenler `baseUrl` ve `adminSifre` (çalıştırmadan önce Postman'de doldur, koleksiyona kaydetme).

| Dosya | Kapsam | İstek / Assertion |
|:--|:--|:--|
| `StajEnvanteri-TumAPI.postman_collection.json` | **17 controller'ın tamamı** | 109 / 148 |

Ön koşul: uygulama ayakta, Postgres bağlı, admin seed'i oluşmuş (`ADMIN_EMAIL` / `ADMIN_SIFRE` ile verdiğin değerler).

```bash
# Postman: Import → Run collection
# Terminal:
npx newman run postman/StajEnvanteri-TumAPI.postman_collection.json \
  --env-var baseUrl=http://localhost:8070
```

Her koşu `runId` ile benzersiz kayıt üretir — aynı veritabanında tekrar tekrar koşulabilir.

---

## StajEnvanteri-TumAPI — klasör sırası

**Sıra önemli.** `00 Kurulum` diğer her şeyi besler; `05 Puanlama` staj2'yi kapatır; `07 Temizlik` veriyi siler.

| # | Klasör | Controller'lar |
|:--|:--|:--|
| 00 | Kurulum (Auth + Provisioning) | `AuthController`, `AdminUserController` |
| 01 | Organizasyon **[KİŞİ-A]** | `DepartmanController`, `DonemController`, `StajyerController`, `SorumluController`, `StajController` |
| 02 | Devam Takibi **[KİŞİ-B / ADIM 1]** | `DevamController`, `AdminDevamController` |
| 03 | Görev / Şablon **[KİŞİ-B / ADIM 2]** | `AdminSablonController`, `SorumluGorevController`, `StajyerGorevController` |
| 04 | Bitiş Bildirimi **[KİŞİ-B / ADIM 3]** | `BildirimController`, `AdminBildirimController` |
| 05 | Puanlama & Kapanış **[KİŞİ-A]** | `SorumluPuanController`, `AdminStajController` |
| 06 | Deprecated | `StajyerApplicationController` |
| 07 | Temizlik **[YIKICI]** | Silme uçları |

### Kurulan test verisi

- **staj1** — çalışma stajı. Devam ve görev testleri burada koşar. Bitiş tarihi bugün+3 (bildirim penceresine düşsün diye).
- **staj2** — puanlama stajı. Puanlama stajı `TAMAMLANDI` yaptığı için ayrı tutuldu; yoksa staj1'in yazma testlerini bozardı.
- **Sorumlu A** (asıl), **B** (yedek), **C** (ilgisiz — yetki izolasyonu testleri için).

### Öne çıkan kabul kriterleri

- `03 / SABLON DEGISSE DE GOREV DEGISMEZ` — şablon 2 maddeden 1 maddeye düşürülür, adı değiştirilir; atanmış görev **değişmez**. Gereksinim 3'ün kanıtı.
- `03 / Onay akışı` — gerekçesiz ret reddedilir; stajyer işi yeniden yaptığında ret gerekçesi **korunur**.
- `02` — girişsiz çıkış, mükerrer gün, gelecek tarih; yedek sorumlu erişebilir, ilgisiz sorumlu 403.
- `04` — bitiş penceresi hesabı ve sorumlu izolasyonu.

---

## Bilinen kusurlar (koleksiyonun ortaya çıkardığı)

**1. Yanlış URL 404 yerine 500 dönüyor.** `GlobalExceptionHandler`'daki `@ExceptionHandler(Exception.class)` Spring'in `NoResourceFoundException`'ını da yutuyor. `06 Deprecated` klasörü bunu belgeliyor — `StajyerApplicationController` kasıtlı kapalı ve sınıfın kendi yorumu "404 döner" diyor, ama 500 dönüyor. Handler ortak altyapı; sahibi netleşince düzeltilmeli.

**2. Yetkilendirme boşluğu.** `/departman`, `/donem`, `/stajyer`, `/sorumlu`, `/staj` uçları `/api/**` dışında olduğu için `SecurityConfig`'in rol kuralları onlara uygulanmıyor — giriş yapmış **her** kullanıcı (stajyer dahil) bunları çağırabiliyor. Koleksiyonda bu uçlar admin token'ıyla çağrılıyor, yani boşluğu test etmiyor. `SecurityConfig` KİŞİ-A'nın alanı.

## Göreli tarih sınırlaması

Prerequest bugüne göreli tarihler üretiyor (`gecmisGun` = bugün−3, `gecmisGun2` = bugün−4,
`dunGun` = bugün−1). Ayın ilk 4–5 gününde koşarsan bu geçmiş günler önceki aya düşer;
`02 / aylık takvim` ve `aylık özet` sorguları (`?yil&ay` = bu ay) onları bulamaz. O günlerde
`ay` değişkenini elle önceki aya çevir ya da ilgili sorguları atla.
