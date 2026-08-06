import { Alert, Chip, MenuItem, Stack, Tab, Tabs, TextField, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { stajlarim } from "../../api/me";
import { DevamimSekmesi } from "./DevamimSekmesi";
import { GorevlerimSekmesi } from "./GorevlerimSekmesi";

/**
 * Stajyer paneli. Ayni kisinin gecmis donemleri AYRI staj kayitlari oldugu icin
 * liste birden fazla satir donebilir; ust tarafta bir secici ile hangisine
 * baktigimizi belirliyoruz.
 */
export function MePage() {
  /** Bos = "kullanici henuz secim yapmadi", varsayilan asagida turetilir. */
  const [seciliHashId, setSeciliHashId] = useState("");
  const [sekme, setSekme] = useState<"gorevler" | "devam">("gorevler");

  const stajlar = useQuery({ queryKey: ["me", "stajlar"], queryFn: stajlarim });

  /**
   * Secim STATE'TEN OKUNMUYOR, turetiliyor.
   *
   * Onceden varsayilan secimi useEffect yapiyordu; efektler boyamadan SONRA
   * calistigi icin verinin geldigi ilk karede value="" kaliyordu: kutu bos,
   * etiket yukarida asili — ekran goruntusundeki hal. Turetince ilk karede
   * dogru staj zaten secili gelir, arada bos an olmaz.
   *
   * Kullanici bir sey sectiginde seciliHashId dolar ve o kazanir. Liste
   * degisip secili staj kaybolursa yine varsayilana duser.
   */
  const staj =
    stajlar.data?.find((s) => s.hashId === seciliHashId) ??
    stajlar.data?.find((s) => s.durum === "DEVAM_EDIYOR") ??
    stajlar.data?.[0];

  if (stajlar.isPending) {
    return <Typography>Yükleniyor...</Typography>;
  }

  if (!stajlar.data?.length) {
    return <Alert severity="info">Kayıtlı bir stajınız görünmüyor.</Alert>;
  }

  return (
    <>
      <Typography variant="h5" component="h1" gutterBottom>
        Stajım
      </Typography>

      <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }} sx={{ mb: 2 }}>
        <TextField
          select
          size="small"
          label="Staj"
          value={staj?.hashId ?? ""}
          onChange={(e) => setSeciliHashId(e.target.value)}
          sx={{ minWidth: 280 }}
        >
          {stajlar.data.map((s) => (
            <MenuItem key={s.hashId} value={s.hashId}>
              {s.donemAd} — {s.departmanAd}
            </MenuItem>
          ))}
        </TextField>
        {staj && (
          <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
            <Chip
              size="small"
              label={staj.durum}
              color={staj.durum === "DEVAM_EDIYOR" ? "primary" : "default"}
            />
            {staj.seviye && <Chip size="small" label={staj.seviye} variant="outlined" />}
            <Typography variant="body2" color="text.secondary">
              Sorumlu: {staj.asilSorumluAdSoyad ?? "-"}
              {staj.yedekSorumluAdSoyad ? ` (yedek: ${staj.yedekSorumluAdSoyad})` : ""}
            </Typography>
          </Stack>
        )}
      </Stack>

      <Tabs
        value={sekme}
        onChange={(_, v) => setSekme(v)}
        sx={{ mb: 3, borderBottom: 1, borderColor: "divider" }}
      >
        <Tab value="gorevler" label="Görevlerim" />
        <Tab value="devam" label="Devam takvimim" />
      </Tabs>

      {staj && sekme === "gorevler" && <GorevlerimSekmesi staj={staj} />}
      {staj && sekme === "devam" && <DevamimSekmesi staj={staj} />}
    </>
  );
}
