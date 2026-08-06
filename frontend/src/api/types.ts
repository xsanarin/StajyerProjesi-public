/**
 * Backend DTO'larinin birebir TypeScript karsiligi.
 * Kaynak: com.example.StajEnvanterProjesi.entity.dto.* (tahmin degil, koddan).
 *
 * Iki kural:
 *  - ID'ler disariya hep hashId (string) olarak acilir; sayisal DB id'si hic gelmez.
 *  - Tarihler ISO string gelir: LocalDate -> "2026-07-27", LocalDateTime -> "2026-07-27T09:15:00".
 */

export type Rol = "ADMIN" | "SORUMLU" | "STAJYER";
export type Seviye = "LEVEL_1" | "LEVEL_2" | "LEVEL_3" | "LEVEL_4" | "LEVEL_5";

/**
 * Seviyeler ARTAN sirada. Backend'deki Seviye enum'unun sirasiyla birebir ayni
 * olmali: yukseltme orada ordinal() ile karsilastiriliyor ve yalnizca ileri
 * gidiyor. Seviye listeleyen her yer bu diziyi kullanir, elle MenuItem yazmaz.
 */
export const SEVIYELER: Seviye[] = ["LEVEL_1", "LEVEL_2", "LEVEL_3", "LEVEL_4", "LEVEL_5"];

/** Bir ustteki seviye; son seviyede (LEVEL_5) null doner. */
export function sonrakiSeviye(mevcut: Seviye | null | undefined): Seviye | null {
  if (!mevcut) {
    return null;
  }
  const sira = SEVIYELER.indexOf(mevcut);
  return sira >= 0 && sira < SEVIYELER.length - 1 ? SEVIYELER[sira + 1] : null;
}
export type StajDurumu = "DEVAM_EDIYOR" | "TAMAMLANDI" | "IPTAL";
export type GorevDurumu = "BEKLIYOR" | "TAMAMLANDI" | "ONAYLANDI" | "REDDEDILDI";

/** Staj.devamGunleri -> Java DayOfWeek; JSON'da enum adi olarak gelir. */
export type HaftaGunu =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

// ---- Auth ----

export type LoginRequest = { email: string; sifre: string };

export type LoginResponse = {
  /** sifreDegistirmeli=true ise null gelir. */
  token: string | null;
  rol: Rol;
  sifreDegistirmeli: boolean;
  mesaj: string | null;
};

export type SifreDegistirRequest = {
  email: string;
  eskiSifre: string;
  /** Backend @Size(min=6) dogruluyor. */
  yeniSifre: string;
};

export type ProvisionResponse = {
  profilHashId: string;
  email: string;
  rol: Rol;
  /** Tek seferlik gecici sifre — kullaniciya bu ekranda gosterilmeli, bir daha alinamaz. */
  geciciSifre: string;
  mesaj: string | null;
};

export type SorumluProvisionRequest = {
  ad: string;
  soyad: string;
  email: string;
  telefon?: string | null;
  departmanHashId?: string | null;
};

export type StajyerProvisionRequest = {
  ad: string;
  soyad: string;
  email: string;
  telefon?: string | null;
  universite?: string | null;
  bolum?: string | null;
  sinif?: number | null;
};

/**
 * Profil duzeltme. Olusturma istegiyle ayni alanlar, ama etkisi farkli: backend
 * e-postayi hem profilde hem LOGIN HESABINDA gunceller, yani buradan gonderilen
 * e-posta kisinin giris kimligini degistirir. Sifre etkilenmez.
 *
 * PUT tam ezme semantigi: form her zaman TUM alanlarla gonderilmeli; gonderilmeyen
 * opsiyonel alan (telefon, departman vs.) backend'de temizlenir.
 */
export type SorumluGuncelleRequest = SorumluProvisionRequest;
export type StajyerGuncelleRequest = StajyerProvisionRequest;

// ---- Departman / Donem ----

export type DepartmanRequest = { ad: string };
export type DepartmanResponse = { hashId: string; ad: string; status: boolean | null };

export type DonemRequest = {
  ad: string;
  baslangicTarihi?: string | null;
  bitisTarihi?: string | null;
};
export type DonemResponse = {
  hashId: string;
  ad: string;
  baslangicTarihi: string | null;
  bitisTarihi: string | null;
  aktif: boolean | null;
};

// ---- Kisiler ----

export type SorumluResponse = {
  hashId: string;
  ad: string;
  soyad: string;
  email: string;
  telefon: string | null;
  departmanHashId: string | null;
  departmanAd: string | null;
};

export type StajyerResponse = {
  hashId: string;
  ad: string;
  soyad: string;
  email: string | null;
  telefon: string | null;
  universite: string | null;
  bolum: string | null;
  sinif: number | null;
  status: boolean | null;
};

// ---- Staj ----

export type StajRequest = {
  stajyerHashId: string;
  donemHashId: string;
  asilSorumluHashId: string;
  yedekSorumluHashId?: string | null;
  departmanHashId: string;
  seviye?: Seviye | null;
  stajBaslangicTarihi?: string | null;
  stajBitisTarihi?: string | null;
  devamGunleri?: HaftaGunu[] | null;
};

/**
 * Acilmis stajin duzeltilmesi. Kismi guncelleme: gonderilmeyen (undefined/null) alan
 * backend'de degismeden kalir. Departman ve yedek sorumlu buradan degismez —
 * onlarin kendi uclari var.
 */
export type StajGuncelleRequest = {
  stajBaslangicTarihi?: string | null;
  stajBitisTarihi?: string | null;
  devamGunleri?: HaftaGunu[] | null;
  asilSorumluHashId?: string | null;
};

export type StajResponse = {
  hashId: string;
  stajyerHashId: string;
  stajyerAdSoyad: string;
  donemHashId: string;
  donemAd: string;
  departmanHashId: string;
  departmanAd: string;
  asilSorumluHashId: string | null;
  asilSorumluAdSoyad: string | null;
  yedekSorumluHashId: string | null;
  yedekSorumluAdSoyad: string | null;
  durum: StajDurumu;
  seviye: Seviye | null;
  stajBaslangicTarihi: string | null;
  stajBitisTarihi: string | null;
  devamGunleri: HaftaGunu[] | null;
};

export type DepartmanDegistirRequest = {
  departmanHashId: string;
  asilSorumluHashId: string;
  /** Opsiyonel: departmanda tek sorumlu varsa staj yedeksiz kalir. */
  yedekSorumluHashId?: string | null;
};

// ---- Devam ----

/**
 * DIKKAT: durum degerleri backend'de TURKCE ve 5 tanedir (DevamServiceImpl.gunSatiri):
 *   TAM       -> giris + cikis var
 *   CIKIS_YOK -> giris var, cikis yok  (takvimde uyari)
 *   GELMEDI   -> planli gun, kayit yok, tarih gecmis
 *   BEKLENIYOR-> planli gun ama henuz gelmedi (gelecek tarih)
 *   PLANSIZ   -> stajin haftalik planinda olmayan gun (ya da staj tarihleri disi)
 */
/** ONAY_BEKLIYOR: stajyer bildirdi, sorumlu onaylamadi — devam yuzdesine sayilmaz (V15). */
export type DevamDurumu =
  | "TAM"
  | "CIKIS_YOK"
  | "ONAY_BEKLIYOR"
  | "GELMEDI"
  | "BEKLENIYOR"
  | "PLANSIZ";

export type DevamResponse = {
  /** Kaydi olmayan gunlerde null — takvim ayin HER gunu icin bir satir dondurur. */
  hashId: string | null;
  tarih: string;
  girisSaati: string | null;
  cikisSaati: string | null;
  manuelMi: boolean | null;
  durum: DevamDurumu;
  planli: boolean | null;
  /** Sorumlu onayladi mi? false ise gun devama sayilmaz. */
  onaylandi: boolean | null;
};

export type DevamOzetResponse = {
  planliGun: number;
  gelinenGun: number;
  devamsizGun: number;
  planDisiGun: number;
  devamYuzdesi: number;
  /** Stajyerin bildirdigi ama onaylanmayan gunler; devamsizGun icinde sayilirlar. */
  onayBekleyenGun: number;
};

export type ManuelDevamRequest = {
  tarih: string;
  girisSaati?: string | null;
  cikisSaati?: string | null;
};

/**
 * Admin devam duzeltme: yalnizca saatler degisir, tarih degismez
 * (yanlis gun icin: sil + yeniden manuel ekle).
 * DIKKAT: LocalTime degil LocalDateTime — `${tarih}T${saat}:00` gonderilmeli.
 */
export type DevamGuncelleRequest = {
  girisSaati?: string | null;
  cikisSaati?: string | null;
};

// ---- Gorev / Sablon ----

export type SablonMaddeResponse = { hashId: string; maddeMetni: string; sira: number | null };

export type SablonResponse = {
  hashId: string;
  ad: string;
  aciklama: string | null;
  departmanHashId: string;
  departmanAd: string;
  seviye: Seviye;
  aktif: boolean | null;
  maddeler: SablonMaddeResponse[];
};

export type SablonRequest = {
  ad: string;
  aciklama?: string | null;
  departmanHashId: string;
  seviye: Seviye;
  maddeler: { maddeMetni: string; sira?: number | null }[];
};

export type IlerlemeResponse = { toplam: number; tamamlanan: number; yuzde: number };

export type StajGorevMaddeResponse = {
  hashId: string;
  maddeMetni: string;
  sira: number | null;
  /** true ise madde sablondan gelmedi; sorumlu bu staja ozel ekledi. */
  ozelMi: boolean | null;
  stajyerNotu: string | null;
  durum: GorevDurumu;
  onayTarihi: string | null;
  onayNotu: string | null;
};

export type StajGorevResponse = {
  hashId: string;
  baslik: string;
  aciklama: string | null;
  seviye: Seviye | null;
  durum: GorevDurumu;
  atanmaTarihi: string | null;
  kaynakSablonHashId: string | null;
  ilerleme: IlerlemeResponse | null;
  maddeler: StajGorevMaddeResponse[];
};

export type SablonAtaRequest = { sablonHashId: string };

/** Yalnizca ileri yonde; mevcut seviyenin gorevleri bitmeden backend yukseltmeye izin vermez. */
export type SeviyeGuncelleRequest = { seviye: Seviye };

export type OzelMaddeRequest = { maddeMetni: string };

/** stajyerNotu gonderilmezse mevcut not korunur; bosaltmak icin bos string. */
export type MaddeTamamlaRequest = { stajyerNotu?: string | null; tamamlandi?: boolean | null };

/** durum yalnizca ONAYLANDI | REDDEDILDI; REDDEDILDI'de onayNotu zorunlu (serviste dogrulanir). */
export type MaddeOnayRequest = { durum: GorevDurumu; onayNotu?: string | null };

// ---- Puanlama ----

export type PuanRequest = { puan: number; yorum?: string | null };
export type PuanDuzeltRequest = { puan: number; yorum?: string | null; gerekce: string };

export type DegerlendirmeResponse = {
  hashId: string;
  stajHashId: string;
  stajyerAdSoyad: string;
  puan: number | null;
  yorum: string | null;
  puanlayanSorumluHashId: string | null;
  duzeltmeGerekcesi: string | null;
  stajDurumu: StajDurumu;
};

// ---- Bildirim ----

export type YaklasanBitisResponse = {
  stajHashId: string;
  stajyerAdSoyad: string;
  departmanAd: string;
  stajBitisTarihi: string;
  gunKalan: number;
  asilSorumluAdSoyad: string | null;
  yedekSorumluAdSoyad: string | null;
};

// ---- Sayfalama ----

/**
 * Sunucu tarafli listelerin ortak zarfi (backend: SayfaResponse<T>).
 * `sayfa` 0'dan baslar; `toplamKayit` filtreye uyan TUM kayitlarin sayisi,
 * sadece bu sayfadakilerin degil.
 */
export type SayfaResponse<T> = {
  icerik: T[];
  sayfa: number;
  boyut: number;
  toplamKayit: number;
  toplamSayfa: number;
};

/** Istatistik ekraninin ozet satiri: departman basina benzersiz stajyer sayisi. */
export type DepartmanIstatistikResponse = {
  departmanAd: string;
  stajyerSayisi: number;
};

// ---- Hata govdesi ----

/** GlobalExceptionHandler.createApiError ciktisinin sekli. */
export type ApiError = {
  status: number;
  exception: { message: string; createTime: string; path: string };
};

// ---- BT talep ----

/**
 * BT talebi. Talep DEPARTMANA baglidir, kisiye degil: bir departmandaki tum
 * sorumlular ayni listeyi gorur. Bu yuzden burada sorumlu alani yoktur.
 *
 * Ilk surumde durum/oncelik/not alanlari YOK — talep tek yonlu: admin olusturur,
 * sorumlu okur.
 */
export type TalepResponse = {
  hashId: string;
  departmanHashId: string;
  departmanAd: string;
  /**
   * Talebin acildigi stajyer (V22). Ikisi de null olabilir: departman geneline
   * acilmis talepte kimse atanmamistir.
   */
  stajyerHashId: string | null;
  stajyerAdSoyad: string | null;
  baslik: string;
  aciklama: string | null;
  /** Backend dd/MM/yyyy HH:mm bicimli metin doner, Date degil. */
  olusturmaTarihi: string;
};

export type TalepRequest = {
  departmanHashId: string;
  /**
   * Opsiyonel. Verilirse stajyerin o departmanda aktif staji olmali (backend
   * dogruluyor). Bos gonderilirse mevcut atama TEMIZLENIR — PUT tam ezme.
   */
  stajyerHashId?: string | null;
  baslik: string;
  aciklama?: string | null;
};
