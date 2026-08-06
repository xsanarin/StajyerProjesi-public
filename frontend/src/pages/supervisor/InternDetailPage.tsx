import { Alert, Button, Chip, Stack, Tab, Tabs, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { benimStajlarim } from "../../api/sorumlu";
import { DevamSekmesi } from "./DevamSekmesi";
import { GorevSekmesi } from "./GorevSekmesi";
import { PuanSekmesi } from "./PuanSekmesi";

/**
 * Tek stajyerin detayi. Staj bilgisini AYRI bir uctan cekmiyoruz: sorumluya acik
 * tek staj ucu liste ucu (GET /api/sorumlu/stajlar); /staj/getById ADMIN'e kilitli.
 * Liste zaten cache'te oldugu icin bu ek istek de dogurmaz.
 */
export function InternDetailPage() {
  const { hashId } = useParams<{ hashId: string }>();
  const [sekme, setSekme] = useState<"devam" | "gorevler" | "puan">("devam");

  const stajlar = useQuery({ queryKey: ["sorumlu", "stajlar"], queryFn: benimStajlarim });
  const staj = stajlar.data?.find((s) => s.hashId === hashId);

  if (stajlar.isPending) {
    return <Typography>Yükleniyor...</Typography>;
  }

  if (!staj) {
    return (
      <>
        <Alert severity="warning" sx={{ mb: 2 }}>
          Bu staj listenizde bulunamadı. Başkasının stajyeri olabilir ya da atamanız kaldırılmış
          olabilir.
        </Alert>
        <Button component={Link} to="/supervisor">
          Listeye Dön
        </Button>
      </>
    );
  }

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 1 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          {staj.stajyerAdSoyad}
        </Typography>
        <Chip
          size="small"
          label={staj.durum}
          color={staj.durum === "DEVAM_EDIYOR" ? "primary" : "default"}
        />
        <Button component={Link} to="/supervisor" size="small">
          Listeye Dön
        </Button>
      </Stack>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        {staj.departmanAd} · {staj.donemAd} · {staj.stajBaslangicTarihi ?? "?"} -{" "}
        {staj.stajBitisTarihi ?? "?"}
      </Typography>

      <Tabs
        value={sekme}
        onChange={(_, v) => setSekme(v)}
        sx={{ mb: 3, borderBottom: 1, borderColor: "divider" }}
      >
        <Tab value="devam" label="Devam" />
        <Tab value="gorevler" label="Görevler" />
        <Tab value="puan" label="Puanlama" />
      </Tabs>

      {sekme === "devam" && (
        <DevamSekmesi
          stajHashId={staj.hashId}
          stajBaslangicTarihi={staj.stajBaslangicTarihi}
          stajDurumu={staj.durum}
        />
      )}
      {sekme === "gorevler" && <GorevSekmesi staj={staj} />}
      {sekme === "puan" && <PuanSekmesi staj={staj} />}
    </>
  );
}
