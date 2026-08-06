import { api } from "./client";
import type {
  DegerlendirmeResponse,
  DevamOzetResponse,
  DevamResponse,
  IlerlemeResponse,
  MaddeOnayRequest,
  ManuelDevamRequest,
  PuanRequest,
  SablonResponse,
  Seviye,
  StajGorevMaddeResponse,
  StajGorevResponse,
  StajResponse,
  TalepResponse,
  YaklasanBitisResponse,
} from "./types";

/**
 * SORUMLU uclari — hepsi /api/sorumlu/** altinda, SecurityConfig'te hasRole('SORUMLU').
 *
 * Ortak kural: sorumlunun KIMLIGI hicbir yerde gonderilmez, backend token'dan
 * (CurrentUser) okur. Bu yuzden burada sorumluHashId parametresi olan tek bir
 * fonksiyon yok — istemcinin "ben kimim" demesine guvenilmiyor.
 */

// ---- Stajlarim ----

/** Giris yapan sorumlunun asil ya da yedek oldugu tum stajlar (gecmis donemler dahil). */
export async function benimStajlarim(): Promise<StajResponse[]> {
  const { data } = await api.get<StajResponse[]>("/api/sorumlu/stajlar");
  return data;
}

// ---- Devam ----

/** Tek tik giris; saat sunucudan alinir, govde yok. */
export async function devamGiris(stajHashId: string): Promise<void> {
  await api.post(`/api/sorumlu/stajlar/${stajHashId}/devam/giris`);
}

export async function devamCikis(stajHashId: string): Promise<void> {
  await api.post(`/api/sorumlu/stajlar/${stajHashId}/devam/cikis`);
}

/** Geriye donuk kayit; olusan satir manuelMi=true olarak isaretlenir. */
export async function devamManuel(stajHashId: string, body: ManuelDevamRequest): Promise<void> {
  await api.post(`/api/sorumlu/stajlar/${stajHashId}/devam/manuel`, body);
}

/**
 * Aylik takvim. yil/ay ZORUNLU query param (backend @RequestParam, varsayilan yok).
 * ay 1-12; JS Date.getMonth() 0-tabanli oldugu icin cagiran tarafta +1 unutulmamali.
 * Donen liste ayin HER gunu icin bir satir icerir (kayit yoksa hashId null).
 */
/** V15 — stajyerin bildirdigi gunu onayla; gun devam yuzdesine sayilmaya baslar. */
export async function devamOnayla(
  stajHashId: string,
  devamHashId: string,
): Promise<DevamResponse> {
  const { data } = await api.put<DevamResponse>(
    `/api/sorumlu/stajlar/${stajHashId}/devam/${devamHashId}/onayla`,
  );
  return data;
}

/** V15 — bildirilen gunu reddet (kayit silinir). Onaylanmis kayit reddedilemez. */
export async function devamReddet(stajHashId: string, devamHashId: string): Promise<void> {
  await api.delete(`/api/sorumlu/stajlar/${stajHashId}/devam/${devamHashId}`);
}

export async function devamTakvimi(
  stajHashId: string,
  yil: number,
  ay: number,
): Promise<DevamResponse[]> {
  const { data } = await api.get<DevamResponse[]>(`/api/sorumlu/stajlar/${stajHashId}/devam`, {
    params: { yil, ay },
  });
  return data;
}

export async function devamOzeti(
  stajHashId: string,
  yil: number,
  ay: number,
): Promise<DevamOzetResponse> {
  const { data } = await api.get<DevamOzetResponse>(
    `/api/sorumlu/stajlar/${stajHashId}/devam/ozet`,
    { params: { yil, ay } },
  );
  return data;
}

// ---- Gorevler ----

/** Stajin departman + seviyesine uyan aktif sablonlar; atama ekranindaki dropdown bunu kullanir. */
export async function onerilenSablonlar(stajHashId: string): Promise<SablonResponse[]> {
  const { data } = await api.get<SablonResponse[]>(
    `/api/sorumlu/stajlar/${stajHashId}/onerilen-sablonlar`,
  );
  return data;
}

/** Sablonu staja kopyalar; sablonun sonraki degisiklikleri atanmis gorevi etkilemez. */
export async function sablonAta(
  stajHashId: string,
  sablonHashId: string,
): Promise<StajGorevResponse> {
  const { data } = await api.post<StajGorevResponse>(
    `/api/sorumlu/stajlar/${stajHashId}/gorevler`,
    { sablonHashId },
  );
  return data;
}

export async function stajGorevleri(stajHashId: string): Promise<StajGorevResponse[]> {
  const { data } = await api.get<StajGorevResponse[]>(
    `/api/sorumlu/stajlar/${stajHashId}/gorevler`,
  );
  return data;
}

/** Seviye yalnizca ileri gider; mevcut seviyenin gorevleri bitmemisse backend reddeder. */
export async function seviyeYukselt(stajHashId: string, seviye: Seviye): Promise<StajResponse> {
  const { data } = await api.patch<StajResponse>(`/api/sorumlu/stajlar/${stajHashId}/seviye`, {
    seviye,
  });
  return data;
}

export async function gorevIlerlemesi(gorevHashId: string): Promise<IlerlemeResponse> {
  const { data } = await api.get<IlerlemeResponse>(`/api/sorumlu/gorevler/${gorevHashId}/ilerleme`);
  return data;
}

/**
 * Sablonda olmayan bir maddeyi bu staja ozel ekler. Sablonun kendisi degismez —
 * sablon ortaktir, degistirilse sonraki tum stajyerlere yansirdi.
 * Gorev tamamlanmissa yeni madde onu tekrar acar (backend durumu tazeler).
 */
export async function ozelMaddeEkle(
  gorevHashId: string,
  maddeMetni: string,
): Promise<StajGorevResponse> {
  const { data } = await api.post<StajGorevResponse>(
    `/api/sorumlu/gorevler/${gorevHashId}/madde`,
    { maddeMetni },
  );
  return data;
}

/**
 * Yanlis eklenen ozel maddeyi siler. Backend yalnizca ozelMi=true ve onaylanmamis
 * maddeye izin verir; sablondan gelen madde snapshot'in parcasi oldugu icin silinemez.
 */
export async function ozelMaddeSil(maddeHashId: string): Promise<StajGorevResponse> {
  const { data } = await api.delete<StajGorevResponse>(`/api/sorumlu/maddeler/${maddeHashId}`);
  return data;
}

/** durum ONAYLANDI | REDDEDILDI; REDDEDILDI'de onayNotu zorunlu, yoksa backend hata verir. */
export async function maddeOnayla(
  maddeHashId: string,
  body: MaddeOnayRequest,
): Promise<StajGorevMaddeResponse> {
  const { data } = await api.patch<StajGorevMaddeResponse>(
    `/api/sorumlu/maddeler/${maddeHashId}/onay`,
    body,
  );
  return data;
}

// ---- Puanlama ----

/**
 * Stajin mevcut degerlendirmesi. Sahiplik kapisi backend'de: asil ya da yedek sorumlu
 * okuyabilir (puanlama yalnizca asilin). Puanlanmamis stajda puan null doner.
 */
export async function degerlendirmem(stajHashId: string): Promise<DegerlendirmeResponse> {
  const { data } = await api.get<DegerlendirmeResponse>(
    `/api/sorumlu/stajlar/${stajHashId}/degerlendirme`,
  );
  return data;
}

/** Staji puanlar ve TAMAMLANDI'ya ceker; kilit acilmadan ikinci kez puanlanamaz (admin acar). */
export async function puanla(
  stajHashId: string,
  body: PuanRequest,
): Promise<DegerlendirmeResponse> {
  const { data } = await api.post<DegerlendirmeResponse>(
    `/api/sorumlu/stajlar/${stajHashId}/puanla`,
    body,
  );
  return data;
}

// ---- Bildirim ----

/** Yalnizca giris yapan sorumlunun stajlari. Admin'in genel listesi ayri uc (admin.ts). */
export async function yaklasanBitisler(gun = 5, arama = ""): Promise<YaklasanBitisResponse[]> {
  const { data } = await api.get<YaklasanBitisResponse[]>("/api/sorumlu/yaklasan-bitisler", {
    params: { gun, q: arama || undefined },
  });
  return data;
}

// ---- BT talepler ----

/**
 * Giris yapan sorumlunun DEPARTMANINA ait BT talepleri.
 * Departman istemciden gonderilmez, backend token'dan cozer — sorumlu baska
 * departmanin listesini isteyemez.
 *
 * Salt okunur: talep olusturma/guncelleme admin yetkisinde, sorumlu ucu yok.
 */
export async function btTaleplerim(): Promise<TalepResponse[]> {
  const { data } = await api.get<TalepResponse[]>("/api/sorumlu/talepler");
  return data;
}
