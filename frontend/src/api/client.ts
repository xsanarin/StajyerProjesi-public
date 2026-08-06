import axios, { AxiosError } from "axios";
import type { ApiError } from "./types";

export const TOKEN_KEY = "staj.token";
export const ROL_KEY = "staj.rol";
export const EMAIL_KEY = "staj.email";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8070",
});

// Her istege token'i ekle. Backend stateless: cookie yok, kimlik her istekte header'da.
api.interceptors.request.use((cfg) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    cfg.headers.Authorization = `Bearer ${token}`;
  }
  return cfg;
});

/**
 * 401 = token yok/gecersiz/suresi dolmus (30 dk) -> oturumu temizleyip login'e at.
 * 403 = token gecerli ama rol yetmiyor; bu bir oturum sorunu DEGIL, kullaniciyi
 * atmayiz, sayfa hatayi toast olarak gosterir.
 *
 * Login ekranindaki hatali sifre de 401 dondurur; oradayken yonlendirme yapmak
 * sonsuz donguye girerdi, o yuzden zaten /login'deysek dokunmuyoruz.
 */
api.interceptors.response.use(
  (r) => r,
  (err: AxiosError<ApiError>) => {
    if (err.response?.status === 401 && !window.location.pathname.startsWith("/login")) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(ROL_KEY);
      localStorage.removeItem(EMAIL_KEY);
      window.location.assign("/login?sebep=oturum");
    }
    return Promise.reject(err);
  },
);

/**
 * Backend hatasini kullaniciya gosterilecek tek satira cevirir.
 * GlobalExceptionHandler govdesi: { status, exception: { message, ... } }.
 * Ag hatasi (backend kapali / CORS reddi) durumunda response hic olusmaz —
 * bu ikisi tarayicida ayirt edilemedigi icin ortak, yol gosteren bir mesaj veriyoruz.
 */
export function hataMesaji(err: unknown, varsayilan = "Beklenmedik bir hata olustu"): string {
  const ax = err as AxiosError<ApiError>;
  const mesaj = ax?.response?.data?.exception?.message;
  if (typeof mesaj === "string" && mesaj.trim()) {
    return mesaj;
  }
  if (ax?.response?.status === 403) {
    return "Bu islem icin yetkiniz yok";
  }
  if (ax?.isAxiosError && !ax.response) {
    return "Sunucuya ulasilamadi (backend 8070'te calisiyor mu? CORS acik mi?)";
  }
  return varsayilan;
}