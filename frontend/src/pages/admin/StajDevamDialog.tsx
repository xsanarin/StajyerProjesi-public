import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Stack,
  Switch,
  TextField,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import {
  adminDevamGuncelle,
  adminDevamManuel,
  adminDevamOzeti,
  adminDevamSil,
  adminDevamTakvimi,
} from "../../api/admin";
import type { DevamResponse, StajResponse } from "../../api/types";
import { AyGezgini, DevamOzetSatiri, DevamTakvimi, saatGoster } from "../../ui/DevamTakvimi";
import { IslemButonu } from "../../ui/IslemButonu";
import { OnayDialog } from "../../ui/OnayDialog";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { useToast } from "../../ui/ToastProvider";

type ManuelForm = { tarih: string; girisSaati: string; cikisSaati: string };
type DuzeltmeForm = { gun: DevamResponse; girisSaati: string; cikisSaati: string };

/**
 * Admin devam mudahalesi. Sorumlu bir devam kaydini yalnizca EKLEYEBILIR; duzeltme ve
 * silme uclari sadece /api/admin/** altinda. Bu dialog o uclarin tek arayuzu.
 */
export function StajDevamDialog({
  staj,
  onKapat,
}: {
  staj: StajResponse | null;
  onKapat: () => void;
}) {
  const toast = useToast();
  const qc = useQueryClient();

  const bugun = new Date();
  const [yil, setYil] = useState(bugun.getFullYear());
  // getMonth() 0-tabanli; backend 1-12 bekliyor.
  const [ay, setAy] = useState(bugun.getMonth() + 1);
  const [planDisiGoster, setPlanDisiGoster] = useState(false);
  const [manuel, setManuel] = useState<ManuelForm | null>(null);
  const [duzeltme, setDuzeltme] = useState<DuzeltmeForm | null>(null);
  const [silinecek, setSilinecek] = useState<DevamResponse | null>(null);

  const stajHashId = staj?.hashId ?? "";
  const acik = staj !== null;

  const takvim = useQuery({
    queryKey: ["admin", "devam", stajHashId, yil, ay],
    queryFn: () => adminDevamTakvimi(stajHashId, yil, ay),
    enabled: acik,
  });
  const ozet = useQuery({
    queryKey: ["admin", "devam-ozet", stajHashId, yil, ay],
    queryFn: () => adminDevamOzeti(stajHashId, yil, ay),
    enabled: acik,
  });

  /** Her mudahale sonrasi hem takvim hem ozet bayatlar. */
  function tazele() {
    qc.invalidateQueries({ queryKey: ["admin", "devam", stajHashId] });
    qc.invalidateQueries({ queryKey: ["admin", "devam-ozet", stajHashId] });
  }

  // Backend girisSaati/cikisSaati alanlarini LocalDateTime bekliyor; "09:15" tek basina
  // parse edilemez, gunun tarihiyle birlestirilmeli.
  const zaman = (tarih: string, saat: string) => (saat ? `${tarih}T${saat}:00` : null);

  const manuelKaydet = useMutation({
    mutationFn: (f: ManuelForm) =>
      adminDevamManuel(stajHashId, {
        tarih: f.tarih,
        girisSaati: zaman(f.tarih, f.girisSaati),
        cikisSaati: zaman(f.tarih, f.cikisSaati),
      }),
    onSuccess: () => {
      tazele();
      setManuel(null);
      toast.basari("Manuel kayıt eklendi.");
    },
    onError: (err) => toast.hata(err, "Manuel kayıt eklenemedi"),
  });

  const duzeltKaydet = useMutation({
    mutationFn: (f: DuzeltmeForm) =>
      adminDevamGuncelle(stajHashId, f.gun.hashId!, {
        girisSaati: zaman(f.gun.tarih, f.girisSaati),
        cikisSaati: zaman(f.gun.tarih, f.cikisSaati),
      }),
    onSuccess: () => {
      tazele();
      setDuzeltme(null);
      toast.basari("Devam kaydı düzeltildi.");
    },
    onError: (err) => toast.hata(err, "Devam kaydı düzeltilemedi"),
  });

  const sil = useMutation({
    mutationFn: (gun: DevamResponse) => adminDevamSil(stajHashId, gun.hashId!),
    onSuccess: () => {
      tazele();
      setSilinecek(null);
      toast.basari("Devam kaydı silindi.");
    },
    onError: (err) => toast.hata(err, "Devam kaydı silinemedi"),
  });

  return (
    <>
      <Dialog open={acik} onClose={onKapat} maxWidth="md" fullWidth>
        {/* Ayni stajyerin birden fazla staji olabiliyor (farkli donem/departman);
            yalnizca ad yazarsak hangi stajin takvimine baktigin anlasilmiyor. */}
        <DialogTitle sx={{ pb: 1 }}>
          Devam Kayıtları — {staj?.stajyerAdSoyad}
          <Typography variant="body2" color="text.secondary">
            {staj?.departmanAd} · {staj?.donemAd} · {staj?.stajBaslangicTarihi ?? "?"} –{" "}
            {staj?.stajBitisTarihi ?? "?"}
          </Typography>
        </DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            Yanlış güne girilmiş kayıt düzeltilemez; kaydı silip doğru güne manuel ekleyin.
            Silme geri alınamaz (kayıt pasifleştirilir).
          </Alert>

          <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap>
            <AyGezgini
              yil={yil}
              ay={ay}
              onDegis={(y, a) => {
                setYil(y);
                setAy(a);
              }}
            />
            <Box sx={{ flexGrow: 1 }} />
            <FormControlLabel
              control={
                <Switch
                  checked={planDisiGoster}
                  onChange={(e) => setPlanDisiGoster(e.target.checked)}
                />
              }
              label="Plan dışı günler"
            />
            <Button
              variant="contained"
              size="small"
              onClick={() =>
                setManuel({
                  tarih: new Date().toISOString().slice(0, 10),
                  girisSaati: "",
                  cikisSaati: "",
                })
              }
            >
              Manuel Kayıt Ekle
            </Button>
          </Stack>

          {ozet.data && (
            <Box sx={{ mb: 2 }}>
              <DevamOzetSatiri ozet={ozet.data} />
            </Box>
          )}

          <DevamTakvimi
            gunler={takvim.data}
            yukleniyor={takvim.isPending}
            planDisiGoster={planDisiGoster}
            stajBaslangicTarihi={staj?.stajBaslangicTarihi}
            /* Kaydi olmayan gunde (hashId null) duzeltilecek/silinecek bir sey yok. */
            satirIslemleri={(g) =>
              g.hashId ? (
                <SatirMenusu
                  islemler={[
                    {
                      etiket: "Saatleri Düzelt",
                      onSecim: () =>
                        setDuzeltme({
                          gun: g,
                          girisSaati: g.girisSaati ? saatGoster(g.girisSaati) : "",
                          cikisSaati: g.cikisSaati ? saatGoster(g.cikisSaati) : "",
                        }),
                    },
                    { etiket: "Sil", onSecim: () => setSilinecek(g), tehlikeli: true },
                  ]}
                />
              ) : null
            }
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={onKapat}>Kapat</Button>
        </DialogActions>
      </Dialog>

      {/* ---- Manuel kayit ---- */}
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
                helperText="Gelecek tarih kabul edilmez."
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
                helperText="Çıkış yazacaksanız giriş de zorunlu."
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

      {/* ---- Saat duzeltme ---- */}
      <Dialog open={duzeltme !== null} onClose={() => setDuzeltme(null)} maxWidth="xs" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (duzeltme) {
              duzeltKaydet.mutate(duzeltme);
            }
          }}
        >
          <DialogTitle>Saatleri Düzelt — {duzeltme?.gun.tarih}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                label="Giriş saati"
                type="time"
                value={duzeltme?.girisSaati ?? ""}
                onChange={(e) => setDuzeltme((d) => (d ? { ...d, girisSaati: e.target.value } : d))}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
              <TextField
                label="Çıkış saati"
                type="time"
                value={duzeltme?.cikisSaati ?? ""}
                onChange={(e) => setDuzeltme((d) => (d ? { ...d, cikisSaati: e.target.value } : d))}
                InputLabelProps={{ shrink: true }}
                fullWidth
                helperText="Çıkış girişten önce olamaz; çıkış varsa giriş zorunlu."
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDuzeltme(null)}>Vazgeç</Button>
            <IslemButonu type="submit" variant="contained" calisiyor={duzeltKaydet.isPending}>
              Kaydet
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      <OnayDialog
        acik={silinecek !== null}
        baslik="Devam kaydı silinsin mi?"
        metin={`${silinecek?.tarih} tarihli kayıt silinecek. Devam yüzdesi yeniden hesaplanır.`}
        onayMetni="Sil"
        calisiyor={sil.isPending}
        onOnay={() => silinecek && sil.mutate(silinecek)}
        onKapat={() => setSilinecek(null)}
      />
    </>
  );
}
