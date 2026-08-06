import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Paper,
  Stack,
  Switch,
  TextField,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import {
  devamCikis,
  devamGiris,
  devamManuel,
  devamOnayla,
  devamOzeti,
  devamReddet,
  devamTakvimi,
} from "../../api/sorumlu";
import type { DevamResponse, StajDurumu } from "../../api/types";
import { AyGezgini, DevamOzetSatiri, DevamTakvimi } from "../../ui/DevamTakvimi";
import { IslemButonu } from "../../ui/IslemButonu";
import { OnayDialog } from "../../ui/OnayDialog";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { useToast } from "../../ui/ToastProvider";

type ManuelForm = { tarih: string; girisSaati: string; cikisSaati: string };

export function DevamSekmesi({
  stajHashId,
  /** null ise takvim bos cikar; DevamTakvimi sebebini yazar. */
  stajBaslangicTarihi,
  stajDurumu,
}: {
  stajHashId: string;
  stajBaslangicTarihi?: string | null;
  stajDurumu: StajDurumu;
}) {
  const toast = useToast();
  const qc = useQueryClient();

  // Kapanmis stajda yazma islemleri backend'de zaten reddediliyor; butonlari da
  // kapatalim ki kullanici bosuna deneyip hata almasin (me/DevamimSekmesi ile ayni desen).
  const saltOkunur = stajDurumu !== "DEVAM_EDIYOR";

  const bugun = new Date();
  const [yil, setYil] = useState(bugun.getFullYear());
  // getMonth() 0-tabanli; backend 1-12 bekliyor.
  const [ay, setAy] = useState(bugun.getMonth() + 1);
  const [planDisiGoster, setPlanDisiGoster] = useState(false);
  const [manuel, setManuel] = useState<ManuelForm | null>(null);
  const [reddedilecek, setReddedilecek] = useState<DevamResponse | null>(null);

  const takvim = useQuery({
    queryKey: ["sorumlu", "devam", stajHashId, yil, ay],
    queryFn: () => devamTakvimi(stajHashId, yil, ay),
  });
  const ozet = useQuery({
    queryKey: ["sorumlu", "devam-ozet", stajHashId, yil, ay],
    queryFn: () => devamOzeti(stajHashId, yil, ay),
  });

  /** Giris/cikis/manuel sonrasi hem takvim hem ozet bayatlar. */
  function tazele() {
    qc.invalidateQueries({ queryKey: ["sorumlu", "devam", stajHashId] });
    qc.invalidateQueries({ queryKey: ["sorumlu", "devam-ozet", stajHashId] });
  }

  const giris = useMutation({
    mutationFn: () => devamGiris(stajHashId),
    onSuccess: () => {
      tazele();
      toast.basari("Giriş kaydedildi.");
    },
    onError: (err) => toast.hata(err, "Giriş kaydedilemedi"),
  });

  const cikis = useMutation({
    mutationFn: () => devamCikis(stajHashId),
    onSuccess: () => {
      tazele();
      toast.basari("Çıkış kaydedildi.");
    },
    onError: (err) => toast.hata(err, "Çıkış kaydedilemedi"),
  });

  const onayla = useMutation({
    mutationFn: (devamHashId: string) => devamOnayla(stajHashId, devamHashId),
    onSuccess: () => {
      tazele();
      toast.basari("Gün onaylandı, devama sayıldı.");
    },
    onError: (err) => toast.hata(err, "Gün onaylanamadı"),
  });

  const reddet = useMutation({
    mutationFn: (devamHashId: string) => devamReddet(stajHashId, devamHashId),
    onSuccess: () => {
      tazele();
      setReddedilecek(null);
      toast.basari("Bildirim reddedildi, kayıt silindi.");
    },
    onError: (err) => toast.hata(err, "Bildirim reddedilemedi"),
  });

  const manuelKaydet = useMutation({
    mutationFn: (f: ManuelForm) =>
      devamManuel(stajHashId, {
        tarih: f.tarih,
        // Backend bu iki alani LocalDateTime bekliyor; saat girdisini gunun tarihiyle
        // birlestirmezsek "09:15" parse edilemez ve istek 400 doner.
        girisSaati: f.girisSaati ? `${f.tarih}T${f.girisSaati}:00` : null,
        cikisSaati: f.cikisSaati ? `${f.tarih}T${f.cikisSaati}:00` : null,
      }),
    onSuccess: () => {
      tazele();
      setManuel(null);
      toast.basari("Manuel kayıt eklendi.");
    },
    onError: (err) => toast.hata(err, "Manuel kayıt eklenemedi"),
  });

  return (
    <>
      {saltOkunur && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Bu staj {stajDurumu} durumunda; devam kayıtları yalnızca görüntülenebilir.
        </Alert>
      )}

      <Paper sx={{ p: 2, mb: 2 }}>
        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap alignItems="center">
          <IslemButonu
            variant="contained"
            onClick={() => giris.mutate()}
            calisiyor={giris.isPending}
            disabled={saltOkunur}
          >
            Giriş Yap
          </IslemButonu>
          <IslemButonu
            variant="outlined"
            onClick={() => cikis.mutate()}
            calisiyor={cikis.isPending}
            disabled={saltOkunur}
          >
            Çıkış Yap
          </IslemButonu>
          <Button
            disabled={saltOkunur}
            onClick={() =>
              setManuel({
                tarih: new Date().toISOString().slice(0, 10),
                girisSaati: "",
                cikisSaati: "",
              })
            }
          >
            Manuel Kayıt
          </Button>
        </Stack>
      </Paper>

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

      {(ozet.data?.onayBekleyenGun ?? 0) > 0 && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          Stajyerin bildirdiği <strong>{ozet.data?.onayBekleyenGun} gün</strong> onayınızı
          bekliyor. Onaylanmayan gün devam yüzdesine sayılmaz.
        </Alert>
      )}

      <DevamTakvimi
        gunler={takvim.data}
        yukleniyor={takvim.isPending}
        planDisiGoster={planDisiGoster}
        stajBaslangicTarihi={stajBaslangicTarihi}
        /* Yalnizca stajyerin bildirdigi (onaylanmamis) gunde ve staj acikken islem var. */
        satirIslemleri={(g) =>
          g.hashId && g.durum === "ONAY_BEKLIYOR" && !saltOkunur ? (
            <SatirMenusu
              islemler={[
                { etiket: "Onayla", onSecim: () => onayla.mutate(g.hashId!) },
                { etiket: "Reddet", onSecim: () => setReddedilecek(g), tehlikeli: true },
              ]}
            />
          ) : null
        }
      />

      <Dialog open={manuel !== null} onClose={() => setManuel(null)} maxWidth="xs" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (manuel) {
              manuelKaydet.mutate(manuel);
            }
          }}
        >
          <DialogTitle>Manuel Devam Kaydı</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                label="Tarih"
                type="date"
                value={manuel?.tarih ?? ""}
                onChange={(e) => setManuel((m) => (m ? { ...m, tarih: e.target.value } : m))}
                InputLabelProps={{ shrink: true }}
                required
                fullWidth
              />
              <TextField
                label="Giriş saati"
                type="time"
                value={manuel?.girisSaati ?? ""}
                onChange={(e) => setManuel((m) => (m ? { ...m, girisSaati: e.target.value } : m))}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
              <TextField
                label="Çıkış saati"
                type="time"
                value={manuel?.cikisSaati ?? ""}
                onChange={(e) => setManuel((m) => (m ? { ...m, cikisSaati: e.target.value } : m))}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setManuel(null)}>Vazgeç</Button>
            <IslemButonu type="submit" variant="contained" calisiyor={manuelKaydet.isPending}>
              Kaydet
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      <OnayDialog
        acik={reddedilecek !== null}
        baslik="Bildirim reddedilsin mi?"
        metin={`${reddedilecek?.tarih} tarihli bildirim silinecek ve gün devamsız sayılacak. Stajyer aynı günü yeniden bildiremez; gerekirse manuel kayıt açmanız gerekir.`}
        onayMetni="Reddet"
        calisiyor={reddet.isPending}
        onOnay={() => reddedilecek?.hashId && reddet.mutate(reddedilecek.hashId)}
        onKapat={() => setReddedilecek(null)}
      />
    </>
  );
}
