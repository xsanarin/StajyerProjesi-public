import { api } from "./client";
import type { LoginRequest, LoginResponse, SifreDegistirRequest } from "./types";

// /api/auth/** SecurityConfig'te permitAll — token gerektirmez.

export async function login(body: LoginRequest): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>("/api/auth/login", body);
  return data;
}

/** Ilk giris / sifirlanmis sifre akisi. Basarili olursa token dolu LoginResponse doner. */
export async function sifreDegistir(body: SifreDegistirRequest): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>("/api/auth/sifre-degistir", body);
  return data;
}