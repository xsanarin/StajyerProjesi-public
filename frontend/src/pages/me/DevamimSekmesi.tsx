import { Alert, Box, FormControlLabel, Paper, Stack, Switch } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import {
  devamCikisBildir,
  devamGirisBildir,
  devamOzetim,
  devamTakvimim,
} from "../../api/me";
import type { StajResponse } from "../../api/types";
import { AyGezgini, DevamOzetSatiri, DevamTakvimi } from "../../ui/DevamTakvimi";
import { IslemButonu } from "../../ui/IslemButonu";
import { useToast } from "../../ui/ToastProvider";

/**
 * Stajyerin kendi devam takvimi.
 *
 * V15 — CIFT BEYAN: stajyer geldigini BILDIRIR, sorumlu ONAYLAR. Bildirilen gun
 * onaylanana kadar devam yuzdesine sayilmaz. Geriye donuk kayit hala sorumlunun isi;
 * stajyer yalnizca bugunu bildirebilir (saat sunucudan alinir).
 */
export function DevamimSekmesi({ staj }: { staj: StajResponse }) {
  const toast = useToast();
  const qc = useQueryClient();

  const bugun = new Date();
  const [yil, setYil] = useState(bugun.getFullYear());
  const [ay, setAy] = useState(bugun.getMonth() + 1);
  const [planDisiGoster, setPlanDisiGoster] = useState(false);

  const stajHashId = staj.hashId;
  // Kapanmis stajda bildirim yapilamaz (backend de reddediyor).
  const saltOkunur = staj.durum !== "DEVAM_EDIYOR";

  const takvim = useQuery({
    queryKey: ["me", "devam", stajHashId, yil, ay],
    queryFn: () => devamTakvimim(stajHashId, yil, ay),
  });
  const ozet = useQuery({
    queryKey: ["me", "devam-ozet", stajHashId, yil, ay],
    queryFn: () => devamOzetim(stajHashId, yil, ay),
  });

  function tazele() {
    qc.invalidateQueries({ queryKey: ["me", "devam", stajHashId] });
    qc.invalidateQueries({ queryKey: ["me", "devam-ozet", stajHashId] });
  }

  const giris = useMutation({
    mutationFn: () => devamGirisBildir(stajHashId),
    onSuccess: () => {
      tazele();
      toast.basari("Giriş bildirildi. Sorumlunuzun onayı bekleniyor.");
    },
    onError: (err) => toast.hata(err, "Giriş bildirilemedi"),
  });

  const cikis = useMutation({
    mutationFn: () => devamCikisBildir(stajHashId),
    onSuccess: () => {
      tazele();
      toast.basari("Çıkış bildirildi. Gün sorumlunuzun onayını bekliyor.");
    },
    onError: (err) => toast.hata(err, "Çıkış bildirilemedi"),
  });

  return (
    <>
      <Alert severity="info" sx={{ mb: 2 }}>
        Geldiğinizde <strong>Giriş Bildir</strong>, ayrılırken <strong>Çıkış Bildir</strong>{" "}
        düğmesine basın. Onay <strong>günün tamamı</strong> içindir: sorumlunuz onayladıktan
        sonra çıkış bildirirseniz gün yeniden onaya düşer. Onaylanmayan gün devam yüzdenize
        sayılmaz. Geçmiş bir günü kendiniz ekleyemezsiniz; eksik gün varsa sorumlunuza bildirin.
      </Alert>

      {!saltOkunur && (
        <Paper sx={{ p: 2, mb: 2 }}>
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            <IslemButonu
              variant="contained"
              onClick={() => giris.mutate()}
              calisiyor={giris.isPending}
            >
              Giriş Bildir
            </IslemButonu>
            <IslemButonu
              variant="outlined"
              onClick={() => cikis.mutate()}
              calisiyor={cikis.isPending}
            >
              Çıkış Bildir
            </IslemButonu>
          </Stack>
        </Paper>
      )}

      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        alignItems={{ sm: "center" }}
        sx={{ mb: 2 }}
      >
        <AyGezgini
          yil={yil}
          ay={ay}
          onDegis={(y, a) => {
            setYil(y);
            setAy(a);
          }}
        />
        <Box sx={{ flexGrow: 1 }}>{ozet.data && <DevamOzetSatiri ozet={ozet.data} />}</Box>
        <FormControlLabel
          control={
            <Switch
              checked={planDisiGoster}
              onChange={(e) => setPlanDisiGoster(e.target.checked)}
              size="small"
            />
          }
          label="Plan dışı günler"
        />
      </Stack>

      <DevamTakvimi
        gunler={takvim.data}
        yukleniyor={takvim.isPending}
        planDisiGoster={planDisiGoster}
        stajBaslangicTarihi={staj.stajBaslangicTarihi}
      />
    </>
  );
}
