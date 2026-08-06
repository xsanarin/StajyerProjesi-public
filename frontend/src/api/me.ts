import { api } from "./client";
import type {
  DevamOzetResponse,
  DevamResponse,
  MaddeTamamlaRequest,
  StajGorevMaddeResponse,
  StajGorevResponse,
  StajResponse,
} from "./types";

/**
 * STAJYER uclari — /api/me/** , SecurityConfig'te hasRole('STAJYER').
 * Kimlik token'dan gelir; stajyer yalnizca kendi stajlarini gorur.
 *
 * Yazma yetkisi dar: stajyer sadece gorev maddesi isaretler. Devam kayitlari
 * (giris/cikis/manuel) burada YOK — okuma bile yalnizca takvim ve ozet.
 */

/** Gecmis donemler dahil; tamamlanmis stajlar listede kalir ama salt-okunurdur. */
export async function stajlarim(): Promise<StajResponse[]> {
  const { data } = await api.get<StajResponse[]>("/api/me/stajlar");
  return data;
}

/** stajHashId ZORUNLU query param — backend'de varsayilan yok, eksikse 400 doner. */
export async function gorevlerim(stajHashId: string): Promise<StajGorevResponse[]> {
  const { data } = await api.get<StajGorevResponse[]>("/api/me/gorevler", {
    params: { stajHashId },
  });
  return data;
}

/**
 * Maddeyi tamamla / geri al. tamamlandi=false maddeyi BEKLIYOR'a dondurur.
 * Not zorunlu degil; gonderilmezse mevcut not korunur.
 */
export async function maddeTamamla(
  maddeHashId: string,
  body: MaddeTamamlaRequest,
): Promise<StajGorevMaddeResponse> {
  const { data } = await api.patch<StajGorevMaddeResponse>(`/api/me/maddeler/${maddeHashId}`, body);
  return data;
}

/** Kendi devam takvimi — salt okunur. yil/ay zorunlu, ay 1-12. */
/**
 * V15 — stajyer kendi gelisini bildirir. Kayit "onay bekliyor" acilir; sorumlu
 * onaylayana kadar devam yuzdesine sayilmaz. Saat sunucudan alinir, govde yok.
 */
export async function devamGirisBildir(stajHashId: string): Promise<void> {
  await api.post(`/api/me/stajlar/${stajHashId}/devam/giris`);
}

export async function devamCikisBildir(stajHashId: string): Promise<void> {
  await api.post(`/api/me/stajlar/${stajHashId}/devam/cikis`);
}

export async function devamTakvimim(
  stajHashId: string,
  yil: number,
  ay: number,
): Promise<DevamResponse[]> {
  const { data } = await api.get<DevamResponse[]>(`/api/me/stajlar/${stajHashId}/devam`, {
    params: { yil, ay },
  });
  return data;
}

export async function devamOzetim(
  stajHashId: string,
  yil: number,
  ay: number,
): Promise<DevamOzetResponse> {
  const { data } = await api.get<DevamOzetResponse>(`/api/me/stajlar/${stajHashId}/devam/ozet`, {
    params: { yil, ay },
  });
  return data;
}
