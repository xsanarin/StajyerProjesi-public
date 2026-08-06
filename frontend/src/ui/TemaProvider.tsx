import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";
import { createContext, useCallback, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";

/**
 * Acik/koyu tema. Secim localStorage'da; kullanici bir kez sectiyse isletim
 * sisteminin tercihi artik ezilmez, cunku kasitli secim sistem ayarindan onceliklidir.
 */

const TEMA_KEY = "staj.tema";

type TemaModu = "light" | "dark";

type TemaDurumu = {
  mod: TemaModu;
  modDegistir: () => void;
};

const TemaContext = createContext<TemaDurumu | null>(null);

function baslangicModu(): TemaModu {
  const kayitli = localStorage.getItem(TEMA_KEY);
  if (kayitli === "light" || kayitli === "dark") {
    return kayitli;
  }
  // Hic secim yapilmadiysa isletim sisteminin tercihini izle.
  return window.matchMedia?.("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

export function TemaProvider({ children }: { children: ReactNode }) {
  const [mod, setMod] = useState<TemaModu>(baslangicModu);

  const modDegistir = useCallback(() => {
    setMod((m) => {
      const yeni = m === "light" ? "dark" : "light";
      localStorage.setItem(TEMA_KEY, yeni);
      return yeni;
    });
  }, []);

  const theme = useMemo(() => {
    const koyu = mod === "dark";

    return createTheme({
      palette: {
        mode: mod,
        // Dolgulu butonun tok mavisi. Satir ici metin butonlari bundan daha acik
        // bir tona cekiliyor (asagida textPrimary).
        primary: { main: koyu ? "#2563eb" : "#1565c0" },
        ...(koyu && {
          // Kati siyah yerine katmanli koyu lacivert: sayfa zemini en koyu, panel
          // ve kartlar bir tik acik — boylece yuzeyler birbirinden ayrisiyor.
          background: { default: "#0b0f19", paper: "#161e2e" },
          divider: "rgba(255,255,255,0.08)",
          // Onceki #f8fafc/#94a3b8 ikilisinde ana metin yeterince one cikmiyordu.
          text: { primary: "#f1f5f9", secondary: "#94a3b8" },
          error: { main: "#f87171" },
          info: { main: "#38bdf8" },
        }),
      },
      shape: { borderRadius: 8 },
      components: {
        // MUI koyu temada Paper'a aydinlatma katmani (backgroundImage) bindirir;
        // birakilirsa yukarida verdigimiz #1f2937 oldugu gibi gorunmez.
        MuiPaper: { styleOverrides: { root: { backgroundImage: "none" } } },

        MuiButton: {
          styleOverrides: {
            // MUI butonlari varsayilan olarak BUYUK HARFE cevirir; tablo satirlarindaki
            // "YEDEK ATA" / "SIL" gibi eylemler boyle bagirir gibi duruyordu. Harf araligi
            // da buyuk harf icin ayarlanmis, duz yaziya gecince geri aliniyor.
            root: { textTransform: "none", letterSpacing: 0 },
            contained: { padding: "8px 16px", borderRadius: 6, fontWeight: 500 },
            // Tablo icindeki metin butonlari dolgulu butonla ayni tonda olunca
            // fazla one cikiyordu; bir tik acik/yumusak renkler ve daha belirgin yazi.
            ...(koyu && {
              containedPrimary: {
                color: "#ffffff",
                "&:hover": { backgroundColor: "#1d4ed8" },
              },
              // Satir ici ikincil eylemler (Duzenle, Yedek Ata, Not...) notr gri
              // duruyor, uzerine gelince renkleniyor — yan yana ucu birden
              // mavi olunca hangisinin ne oldugu okunmuyordu.
              textPrimary: {
                color: "#cbd5e1",
                fontWeight: 500,
                "&:hover": { color: "#60a5fa" },
              },
              // Vurgulu/olumlu eylem (Aktif Yap) ayri renkte.
              textInfo: { color: "#38bdf8", fontWeight: 500 },
              // Yikici eylem her zaman kirmizi; hover'a birakilmaz.
              textError: { color: "#f87171", fontWeight: 500 },
            }),
          },
        },

        MuiTableContainer: {
          styleOverrides: {
            root: ({ theme }) => ({ border: `1px solid ${theme.palette.divider}` }),
          },
        },

        MuiTableCell: {
          styleOverrides: {
            // Satir ayrimi blok koyulugu ile degil, ince alt cizgi ile.
            root: ({ theme }) => ({
              borderBottom: `1px solid ${
                koyu ? "rgba(255,255,255,0.05)" : theme.palette.divider
              }`,
            }),
            head: ({ theme }) => ({
              fontSize: 12,
              fontWeight: 600,
              textTransform: "uppercase",
              letterSpacing: "0.05em",
              color: theme.palette.text.secondary,
              backgroundColor: koyu ? "rgba(255,255,255,0.05)" : theme.palette.action.hover,
            }),
            // Veri hucreleri: MUI varsayilani body2 (0.875rem) ama rengi devralip
            // sonuk kalabiliyordu; ana metin rengi ve agirligi acikca veriliyor.
            body: ({ theme }) => ({
              fontSize: 14,
              fontWeight: 400,
              color: theme.palette.text.primary,
            }),
          },
        },

        MuiTableRow: {
          styleOverrides: {
            // Hover'i palette.action.hover uzerinden degistirmiyoruz: o deger sol
            // menu ogelerinde de kullaniliyor, orada bu kadar sonuk olmamali.
            root: {
              ...(koyu && {
                "&.MuiTableRow-hover:hover": { backgroundColor: "rgba(255,255,255,0.04)" },
              }),
            },
          },
        },

        // Secili menu ogesi: dolgu tek basina yeterince belli olmuyordu, sola
        // parlak mavi seritte ekleniyor. paddingLeft 16 -> 13, 3px kenarlikla
        // toplam yine 16 kalsin ve yazi kaymasin diye.
        MuiListItemButton: {
          styleOverrides: {
            root: {
              ...(koyu && {
                "&.Mui-selected": {
                  backgroundColor: "#1e293b",
                  borderLeft: "3px solid #3b82f6",
                  paddingLeft: 13,
                  "&:hover": { backgroundColor: "#1e293b" },
                },
              }),
            },
          },
        },

        // Girdi alanlari yuzeyden bir tik daha koyu — kart uzerinde cukur duruyor.
        MuiOutlinedInput: {
          styleOverrides: {
            root: {
              ...(koyu && {
                backgroundColor: "#0f172a",
                "& .MuiOutlinedInput-notchedOutline": {
                  borderColor: "rgba(255,255,255,0.12)",
                },
              }),
            },
            input: ({ theme }) => ({
              ...(koyu && {
                // MUI placeholder'i opacity ile soluklastirir; rengi uygulamak
                // icin opacity'yi 1'e cekmek sart.
                "&::placeholder": { color: "#64748b", opacity: 1 },
              }),
              /**
               * Chrome otomatik doldurulan alana kendi zeminini basar (koyu temada
               * mavi, acik temada #e8f0fe) ve background-color ile ezilemez. Tek yol
               * alani ic golgeyle boyamak; metin rengi de -webkit-text-fill-color
               * ister. Her iki temada da gecerli.
               */
              "&:-webkit-autofill, &:-webkit-autofill:hover, &:-webkit-autofill:focus": {
                WebkitBoxShadow: `0 0 0 100px ${
                  koyu ? "#0f172a" : theme.palette.background.paper
                } inset`,
                WebkitTextFillColor: theme.palette.text.primary,
                caretColor: theme.palette.text.primary,
              },
            }),
          },
        },

        MuiInputLabel: {
          styleOverrides: {
            root: {
              // Girdi zemini opak oldugu icin ustte yuzen etiketi ortuyordu;
              // etiket bir kat yukari alinmali.
              zIndex: 1,
              ...(koyu && {
                color: "#94a3b8",
                "&.Mui-focused": { color: "#60a5fa" },
              }),
            },
          },
        },
      },
    });
  }, [mod]);

  const deger = useMemo<TemaDurumu>(() => ({ mod, modDegistir }), [mod, modDegistir]);

  return (
    <TemaContext.Provider value={deger}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </TemaContext.Provider>
  );
}

export function useTema(): TemaDurumu {
  const ctx = useContext(TemaContext);
  if (!ctx) {
    throw new Error("useTema yalnizca TemaProvider icinde kullanilabilir");
  }
  return ctx;
}
