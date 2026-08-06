import { createContext, useCallback, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { EMAIL_KEY, ROL_KEY, TOKEN_KEY } from "../api/client";
import type { LoginResponse, Rol } from "../api/types";

/**
 * Oturum durumu. Backend stateless (JWT, 30 dk) — sunucuda oturum yok, tek
 * kaynak localStorage. Sekme yenilenince state buradan geri kurulur.
 *
 * Token'i axios interceptor'i (api/client.ts) dogrudan localStorage'dan okur;
 * bu context onun ustunde yalnizca React tarafinin gorunur durumunu tutar.
 * Ikisi ayni anahtarlari kullanir, tek yazma noktasi asagidaki oturumAc/Kapat.
 */

type Oturum = { token: string; rol: Rol; email: string };

type AuthDurumu = {
  oturum: Oturum | null;
  girisYapildi: boolean;
  /** Login/sifre degistirme yaniti token dolu geldiginde cagrilir. */
  oturumAc: (yanit: LoginResponse, email: string) => void;
  oturumKapat: () => void;
};

const AuthContext = createContext<AuthDurumu | null>(null);

function depodanOku(): Oturum | null {
  const token = localStorage.getItem(TOKEN_KEY);
  const rol = localStorage.getItem(ROL_KEY) as Rol | null;
  const email = localStorage.getItem(EMAIL_KEY);
  if (!token || !rol) {
    return null;
  }
  return { token, rol, email: email ?? "" };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [oturum, setOturum] = useState<Oturum | null>(depodanOku);

  const oturumAc = useCallback((yanit: LoginResponse, email: string) => {
    // Cagiran taraf sifreDegistirmeli akisini zaten ayirdi; yine de token'siz
    // bir yanitla buraya dusulurse sessizce bozuk oturum yazmayalim.
    if (!yanit.token) {
      return;
    }
    localStorage.setItem(TOKEN_KEY, yanit.token);
    localStorage.setItem(ROL_KEY, yanit.rol);
    localStorage.setItem(EMAIL_KEY, email);
    setOturum({ token: yanit.token, rol: yanit.rol, email });
  }, []);

  const oturumKapat = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROL_KEY);
    localStorage.removeItem(EMAIL_KEY);
    setOturum(null);
  }, []);

  const deger = useMemo<AuthDurumu>(
    () => ({ oturum, girisYapildi: oturum !== null, oturumAc, oturumKapat }),
    [oturum, oturumAc, oturumKapat],
  );

  return <AuthContext.Provider value={deger}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthDurumu {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth yalnizca AuthProvider icinde kullanilabilir");
  }
  return ctx;
}

/** Role gore acilis sayfasi — login sonrasi ve "/" yonlendirmesi ayni tabloyu kullanir. */
export function rolAnaSayfasi(rol: Rol): string {
  switch (rol) {
    case "ADMIN":
      return "/admin";
    case "SORUMLU":
      return "/supervisor";
    case "STAJYER":
      return "/me";
  }
}