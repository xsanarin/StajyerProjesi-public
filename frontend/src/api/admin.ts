import { api } from "./client";
import type {
  DegerlendirmeResponse,
  DepartmanIstatistikResponse,
  DepartmanRequest,
  DepartmanResponse,
  DevamGuncelleRequest,
  DevamOzetResponse,
  DevamResponse,
  DonemRequest,
  DonemResponse,
  ManuelDevamRequest,
  ProvisionResponse,
  SablonRequest,
  SablonResponse,
  SayfaResponse,
  SorumluGuncelleRequest,
  SorumluProvisionRequest,
  SorumluResponse,
  StajGuncelleRequest,
  StajRequest,
  StajResponse,
  StajyerGuncelleRequest,
  StajyerProvisionRequest,
  StajyerResponse,
  TalepRequest,
  TalepResponse,
  YaklasanBitisResponse,
} from "./types";

/**
 * ADMIN uclari. Iki grup var ve yollari tutarsiz — bu backend'in gercegi, sadelestirilemez:
 *   - /api/admin/**            : yeni uclar
 *   - /departman, /donem, ...  : auth'tan once yazilmis "ciplak" uclar, SecurityConfig'te
 *                                ADMIN'e kilitli. Silme islemleri burada DELETE degil
 *                                PUT /delete/{hashId} (staj hariç, o gerçekten DELETE).
 */

// ---- Departman ----

export async function departmanlariGetir(): Promise<DepartmanResponse[]> {
  const { data } = await api.get<DepartmanResponse[]>("/departman/getAll");
  return data;
}

export async function departmanKaydet(body: DepartmanRequest): Promise<DepartmanResponse> {
  const { data } = await api.post<DepartmanResponse>("/departman/save", body);
  return data;
}

export async function departmanGuncelle(
  hashId: string,
  body: DepartmanRequest,
): Promise<DepartmanResponse> {
  const { data } = await api.put<DepartmanResponse>(`/departman/update/${hashId}`, body);
  return data;
}

/** Backend'de silme PUT ile yapiliyor (soft delete). */
export async function departmanSil(hashId: string): Promise<void> {
  await api.put(`/departman/delete/${hashId}`);
}

// ---- Donem ----

export async function donemleriGetir(): Promise<DonemResponse[]> {
  const { data } = await api.get<DonemResponse[]>("/donem/getAll");
  return data;
}

export async function donemKaydet(body: DonemRequest): Promise<DonemResponse> {
  const { data } = await api.post<DonemResponse>("/donem/save", body);
  return data;
}

export async function donemGuncelle(hashId: string, body: DonemRequest): Promise<DonemResponse> {
  const { data } = await api.put<DonemResponse>(`/donem/update/${hashId}`, body);
  return data;
}

/** Bir donemi aktif yapar, digerlerini pasife ceker. */
export async function donemAktifYap(hashId: string): Promise<DonemResponse> {
  const { data } = await api.put<DonemResponse>(`/donem/aktifYap/${hashId}`);
  return data;
}

export async function donemSil(hashId: string): Promise<void> {
  await api.put(`/donem/delete/${hashId}`);
}

// ---- Kisiler ----

export async function sorumlulariGetir(): Promise<SorumluResponse[]> {
  const { data } = await api.get<SorumluResponse[]>("/sorumlu/getAll");
  return data;
}

/**
 * DIKKAT: sayfalama yok, tum stajyerleri ceker. Liste ekranlari stajyerAra
 * kullanmali; bu fonksiyon staj acma dialogundaki acilir liste icin duruyor.
 */
export async function stajyerleriGetir(): Promise<StajyerResponse[]> {
  const { data } = await api.get<StajyerResponse[]>("/stajyer/getAll");
  return data;
}

/** Sayfali + aramali stajyer listesi. Bos terim = filtre yok. */
export async function stajyerAra(
  arama: string,
  sayfa: number,
  boyut: number,
): Promise<SayfaResponse<StajyerResponse>> {
  const { data } = await api.get<SayfaResponse<StajyerResponse>>("/stajyer/ara", {
    params: { q: arama || undefined, sayfa, boyut },
  });
  return data;
}

/** Profil + login hesabi birlikte olusur; yanitta tek seferlik gecici sifre gelir. */
export async function sorumluOlustur(body: SorumluProvisionRequest): Promise<ProvisionResponse> {
  const { data } = await api.post<ProvisionResponse>("/api/admin/sorumlular", body);
  return data;
}

export async function stajyerOlustur(body: StajyerProvisionRequest): Promise<ProvisionResponse> {
  const { data } = await api.post<ProvisionResponse>("/api/admin/stajyerler", body);
  return data;
}

/**
 * Sorumlu bilgilerini duzeltir. E-posta degistirilirse backend login hesabini da
 * ayni islemde gunceller (kisi yeni e-postayla girer, sifresi degismez).
 */
export async function sorumluGuncelle(
  hashId: string,
  body: SorumluGuncelleRequest,
): Promise<SorumluResponse> {
  const { data } = await api.put<SorumluResponse>(`/sorumlu/update/${hashId}`, body);
  return data;
}

/** Stajyer icin ayni: e-posta degisirse login kimligi de guncellenir. */
export async function stajyerGuncelle(
  hashId: string,
  body: StajyerGuncelleRequest,
): Promise<StajyerResponse> {
  const { data } = await api.put<StajyerResponse>(`/stajyer/update/${hashId}`, body);
  return data;
}

/** Sorumluyu pasiflestirir (terfi mantigi tetiklenir). Yedeksiz aktif stajyeri varsa backend engeller. */
export async function sorumluAyril(hashId: string): Promise<void> {
  await api.delete(`/api/admin/sorumlular/${hashId}`);
}

// ---- Staj ----

/**
 * DIKKAT: sayfalama yok, tum stajlari ceker. Liste ekranlari stajAra kullanmali;
 * bu fonksiyon kucuk ic kullanimlar icin duruyor.
 */
export async function stajlariGetir(): Promise<StajResponse[]> {
  const { data } = await api.get<StajResponse[]>("/staj/getAll");
  return data;
}

/** Sayfali + aramali staj listesi. Bos terim = filtre yok. */
export async function stajAra(
  arama: string,
  sayfa: number,
  boyut: number,
): Promise<SayfaResponse<StajResponse>> {
  const { data } = await api.get<SayfaResponse<StajResponse>>("/staj/ara", {
    params: { q: arama || undefined, sayfa, boyut },
  });
  return data;
}

/**
 * Departman basina benzersiz stajyer sayisi — istatistik ekraninin ozet ucu.
 * Sayim SQL'de yapiliyor, tum staj listesi cekilmiyor.
 */
export async function departmanIstatistigi(
  sadeceAktif: boolean,
): Promise<DepartmanIstatistikResponse[]> {
  const { data } = await api.get<DepartmanIstatistikResponse[]>("/staj/istatistik/departman", {
    params: { sadeceAktif },
  });
  return data;
}

export async function stajKaydet(body: StajRequest): Promise<StajResponse> {
  const { data } = await api.post<StajResponse>("/staj/save", body);
  return data;
}

/** Tarih / devam plani / asil sorumlu duzeltmesi. Yalnizca DEVAM_EDIYOR stajda calisir. */
export async function stajGuncelle(
  hashId: string,
  body: StajGuncelleRequest,
): Promise<StajResponse> {
  const { data } = await api.put<StajResponse>(`/staj/update/${hashId}`, body);
  return data;
}

export async function stajSil(hashId: string): Promise<void> {
  await api.delete(`/staj/delete/${hashId}`);
}

export async function yedekSorumluAta(
  stajHashId: string,
  sorumluHashId: string,
): Promise<StajResponse> {
  const { data } = await api.put<StajResponse>(`/api/admin/stajlar/${stajHashId}/yedek-sorumlu`, {
    sorumluHashId,
  });
  return data;
}

/**
 * Yanlis departmanla acilmis stajin duzeltilmesi. Departman + asil + yedek TEK istekte
 * gider: eslesme kurali geregi departman degisince mevcut sorumlular gecersizlesir,
 * ayri adimlara bolunse her adim digerinin kuralina takilirdi.
 */
export async function stajDepartmanDegistir(
  stajHashId: string,
  body: {
    departmanHashId: string;
    asilSorumluHashId: string;
    yedekSorumluHashId?: string | null;
  },
): Promise<StajResponse> {
  const { data } = await api.put<StajResponse>(
    `/api/admin/stajlar/${stajHashId}/departman`,
    body,
  );
  return data;
}

// ---- Admin devam mudahalesi ----
// Sorumlu devam kaydini yalnizca EKLEYEBILIR; duzeltme/silme sadece bu uclarda.

/** yil/ay zorunlu. Donen liste ayin HER gunu icin bir satir icerir (kayit yoksa hashId null). */
export async function adminDevamTakvimi(
  stajHashId: string,
  yil: number,
  ay: number,
): Promise<DevamResponse[]> {
  const { data } = await api.get<DevamResponse[]>(`/api/admin/stajlar/${stajHashId}/devam`, {
    params: { yil, ay },
  });
  return data;
}

export async function adminDevamOzeti(
  stajHashId: string,
  yil: number,
  ay: number,
): Promise<DevamOzetResponse> {
  const { data } = await api.get<DevamOzetResponse>(
    `/api/admin/stajlar/${stajHashId}/devam/ozet`,
    { params: { yil, ay } },
  );
  return data;
}

export async function adminDevamManuel(
  stajHashId: string,
  body: ManuelDevamRequest,
): Promise<void> {
  await api.post(`/api/admin/stajlar/${stajHashId}/devam/manuel`, body);
}

export async function adminDevamGuncelle(
  stajHashId: string,
  devamHashId: string,
  body: DevamGuncelleRequest,
): Promise<DevamResponse> {
  const { data } = await api.put<DevamResponse>(
    `/api/admin/stajlar/${stajHashId}/devam/${devamHashId}`,
    body,
  );
  return data;
}

export async function adminDevamSil(stajHashId: string, devamHashId: string): Promise<void> {
  await api.delete(`/api/admin/stajlar/${stajHashId}/devam/${devamHashId}`);
}

/**
 * Stajin mevcut degerlendirmesi. Puanlanmamis stajda hata DEGIL, puan'i null olan
 * bir govde doner — "henuz puanlanmamis" ile "staj yok" ayirt edilebilsin diye.
 */
export async function adminDegerlendirmeGetir(stajHashId: string): Promise<DegerlendirmeResponse> {
  const { data } = await api.get<DegerlendirmeResponse>(
    `/api/admin/stajlar/${stajHashId}/degerlendirme`,
  );
  return data;
}

export async function adminPuanDuzelt(
  stajHashId: string,
  body: { puan: number; yorum?: string | null; gerekce: string },
): Promise<DegerlendirmeResponse> {
  const { data } = await api.put<DegerlendirmeResponse>(
    `/api/admin/stajlar/${stajHashId}/puan`,
    body,
  );
  return data;
}

export async function adminKilidiAc(stajHashId: string): Promise<DegerlendirmeResponse> {
  const { data } = await api.put<DegerlendirmeResponse>(
    `/api/admin/stajlar/${stajHashId}/kilidi-ac`,
  );
  return data;
}

// ---- Gorev sablonlari ----

/**
 * departmanHashId verilirse liste o departmanla sinirlanir. Sablon ekrani her zaman
 * tek bir departman gosterdigi icin bu parametreyi bos birakmaz.
 */
export async function sablonlariGetir(
  sadeceAktif = false,
  departmanHashId?: string,
): Promise<SablonResponse[]> {
  const { data } = await api.get<SablonResponse[]>("/api/admin/sablonlar", {
    params: { sadeceAktif, ...(departmanHashId ? { departmanHashId } : {}) },
  });
  return data;
}

export async function sablonOlustur(body: SablonRequest): Promise<SablonResponse> {
  const { data } = await api.post<SablonResponse>("/api/admin/sablonlar", body);
  return data;
}

/**
 * Guncelleme sablonun KENDISINI degistirir; daha once bu sablondan atanmis gorevler
 * etkilenmez (atama sirasinda kopyalanir).
 */
export async function sablonGuncelle(hashId: string, body: SablonRequest): Promise<SablonResponse> {
  const { data } = await api.put<SablonResponse>(`/api/admin/sablonlar/${hashId}`, body);
  return data;
}

/** Sablon silinmez, pasiflestirilir — gecmis atamalarin izi korunsun diye. */
export async function sablonPasiflestir(hashId: string): Promise<void> {
  await api.delete(`/api/admin/sablonlar/${hashId}`);
}

// ---- Bildirim ----

export async function adminYaklasanBitisler(
  gun = 5,
  arama = "",
): Promise<YaklasanBitisResponse[]> {
  const { data } = await api.get<YaklasanBitisResponse[]>("/api/admin/bildirim/yaklasan-bitisler", {
    params: { gun, q: arama || undefined },
  });
  return data;
}
// ---- BT talepler ----
// Olusturma/guncelleme/silme yalnizca admin yetkisinde; sorumlu tarafinda okuma
// ucu var (api/sorumlu.ts -> btTaleplerim).

export async function talepleriGetir(): Promise<TalepResponse[]> {
  const { data } = await api.get<TalepResponse[]>("/api/admin/talepler");
  return data;
}

/**
 * Talep dialogundaki kisi seciminin kaynagi: secilen departmanda STAJI DEVAM
 * EDEN stajyerler. Bitmis/iptal stajin sahibi gelmez.
 *
 * stajyerleriGetir() kullanilamaz — o TUM stajyerleri dondurur, departman ise
 * kisiye degil staja bagli oldugu icin istemcide suzulemez.
 */
export async function talepDepartmanStajyerleri(
  departmanHashId: string,
): Promise<StajyerResponse[]> {
  const { data } = await api.get<StajyerResponse[]>("/api/admin/talepler/stajyerler", {
    params: { departmanHashId },
  });
  return data;
}

export async function talepKaydet(body: TalepRequest): Promise<TalepResponse> {
  const { data } = await api.post<TalepResponse>("/api/admin/talepler", body);
  return data;
}

export async function talepGuncelle(hashId: string, body: TalepRequest): Promise<TalepResponse> {
  const { data } = await api.put<TalepResponse>(`/api/admin/talepler/${hashId}`, body);
  return data;
}

/** Soft delete: kayit veritabaninda kalir, listelerden dusar. */
export async function talepSil(hashId: string): Promise<void> {
  await api.delete(`/api/admin/talepler/${hashId}`);
}
