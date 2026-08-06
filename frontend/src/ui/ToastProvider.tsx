import { Alert, Snackbar } from "@mui/material";
import { createContext, useCallback, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { hataMesaji } from "../api/client";

/**
 * Tek noktadan bildirim. Sayfalar hata govdesini kendileri acmasin diye
 * `hata(err)` backend mesajini (GlobalExceptionHandler) cikaran yardimcidan gecer.
 */

type ToastTuru = "success" | "error" | "info";
type Toast = { mesaj: string; tur: ToastTuru };

type ToastDurumu = {
  basari: (mesaj: string) => void;
  bilgi: (mesaj: string) => void;
  /** Axios hatasini kullaniciya gosterilecek tek satira cevirip gosterir. */
  hata: (err: unknown, varsayilan?: string) => void;
};

const ToastContext = createContext<ToastDurumu | null>(null);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toast, setToast] = useState<Toast | null>(null);

  const deger = useMemo<ToastDurumu>(
    () => ({
      basari: (mesaj: string) => setToast({ mesaj, tur: "success" }),
      bilgi: (mesaj: string) => setToast({ mesaj, tur: "info" }),
      hata: (err: unknown, varsayilan?: string) =>
        setToast({ mesaj: hataMesaji(err, varsayilan), tur: "error" }),
    }),
    [],
  );

  const kapat = useCallback(() => setToast(null), []);

  return (
    <ToastContext.Provider value={deger}>
      {children}
      <Snackbar
        open={toast !== null}
        autoHideDuration={toast?.tur === "error" ? 8000 : 4000}
        onClose={kapat}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        {/* Snackbar cocugunun her zaman tek eleman olmasi gerekiyor; toast null iken
            Snackbar zaten kapali ama React yine de cocugu render eder. */}
        <Alert severity={toast?.tur ?? "info"} variant="filled" onClose={kapat} sx={{ width: "100%" }}>
          {toast?.mesaj ?? ""}
        </Alert>
      </Snackbar>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastDurumu {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    throw new Error("useToast yalnizca ToastProvider icinde kullanilabilir");
  }
  return ctx;
}
