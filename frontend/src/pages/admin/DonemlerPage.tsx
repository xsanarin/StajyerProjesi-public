import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  MenuItem,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import {
  donemAktifYap,
  donemGuncelle,
  donemKaydet,
  donemSil,
  donemleriGetir,
} from "../../api/admin";
import type { DonemResponse } from "../../api/types";
import { IslemButonu } from "../../ui/IslemButonu";
import { OnayDialog } from "../../ui/OnayDialog";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";
import { useToast } from "../../ui/ToastProvider";

type Form = {
  hashId: string | null;
  ad: string;
  baslangicTarihi: string;
  bitisTarihi: string;
};

const bosForm: Form = { hashId: null, ad: "", baslangicTarihi: "", bitisTarihi: "" };

/**
 * Donem adi serbest metin degil: backend de ayni uclu kumeyi dogruluyor
 * (DonemServiceImpl.GECERLI_ADLAR + V16 CHECK kisiti). Burasi degisirse orasi da degismeli.
 */
const DONEM_ADLARI = ["Yaz", "Güz", "Bahar"] as const;

export function DonemlerPage() {
  const toast = useToast();
  const qc = useQueryClient();

  const liste = useQuery({ queryKey: ["donemler"], queryFn: donemleriGetir });

  const [form, setForm] = useState<Form | null>(null);
  const [silinecek, setSilinecek] = useState<DonemResponse | null>(null);
  const iskeletGoster = useGecikmeliGoster(liste.isPending);
  const kullanilanAdlar = useMemo(
    () => new Set((liste.data ?? []).map((d) => d.ad)),
    [liste.data],
  );
  // Uc ad da acilmissa yeni donem eklenemez.
  const hepsiAcik = DONEM_ADLARI.every((ad) => kullanilanAdlar.has(ad));

  // Bos tarih alanini "" degil null gondermeliyiz; backend LocalDate bekliyor,
  // bos string parse hatasi verir.
  const govde = (f: Form) => ({
    ad: f.ad,
    baslangicTarihi: f.baslangicTarihi || null,
    bitisTarihi: f.bitisTarihi || null,
  });

  const kaydet = useMutation({
    mutationFn: (f: Form) =>
      f.hashId ? donemGuncelle(f.hashId, govde(f)) : donemKaydet(govde(f)),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["donemler"] });
      setForm(null);
      toast.basari("Dönem kaydedildi.");
    },
    onError: (err) => toast.hata(err, "Dönem kaydedilemedi"),
  });

  // Tek aktif donem kurali backend'de; burada sadece tetikliyoruz ve listeyi tazeliyoruz.
  const aktifYap = useMutation({
    mutationFn: donemAktifYap,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["donemler"] });
      toast.basari("Aktif dönem değiştirildi.");
    },
    onError: (err) => toast.hata(err, "Dönem aktif yapılamadı"),
  });

  const sil = useMutation({
    mutationFn: donemSil,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["donemler"] });
      setSilinecek(null);
      toast.basari("Dönem silindi.");
    },
    onError: (err) => toast.hata(err, "Dönem silinemedi"),
  });

  return (
    <>
      <Stack direction="row" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          Dönemler
        </Typography>
        <Button
          variant="contained"
          onClick={() => setForm(bosForm)}
          disabled={hepsiAcik}
          title={hepsiAcik ? "Yaz, Güz ve Bahar zaten tanımlı." : undefined}
        >
          Yeni Dönem
        </Button>
      </Stack>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Ad</TableCell>
              <TableCell>Başlangıç</TableCell>
              <TableCell>Bitiş</TableCell>
              <TableCell>Durum</TableCell>
              <TableCell align="right" width={56} />
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={5} />}
            {liste.data?.map((d) => (
              <TableRow key={d.hashId} hover>
                <TableCell>{d.ad}</TableCell>
                <TableCell>{d.baslangicTarihi ?? "-"}</TableCell>
                <TableCell>{d.bitisTarihi ?? "-"}</TableCell>
                <TableCell>
                  {d.aktif ? <Chip label="Aktif" color="success" size="small" /> : "-"}
                </TableCell>
                <TableCell align="right">
                  <SatirMenusu
                    islemler={[
                      {
                        etiket: "Aktif Yap",
                        onSecim: () => aktifYap.mutate(d.hashId),
                        // Zaten aktif olan donemde anlamsiz. aktif null olabiliyor.
                        gizli: d.aktif === true,
                      },
                      {
                        etiket: "Düzenle",
                        onSecim: () =>
                          setForm({
                            hashId: d.hashId,
                            ad: d.ad,
                            baslangicTarihi: d.baslangicTarihi ?? "",
                            bitisTarihi: d.bitisTarihi ?? "",
                          }),
                      },
                      { etiket: "Sil", onSecim: () => setSilinecek(d), tehlikeli: true },
                    ]}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={form !== null} onClose={() => setForm(null)} maxWidth="xs" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (form) {
              kaydet.mutate(form);
            }
          }}
        >
          <DialogTitle>{form?.hashId ? "Dönemi Düzenle" : "Yeni Dönem"}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                select
                label="Dönem adı"
                value={form?.ad ?? ""}
                onChange={(e) => setForm((f) => (f ? { ...f, ad: e.target.value } : f))}
                required
                autoFocus
                fullWidth
                helperText="Dönem adı bu üçüyle sınırlı; her biri yalnızca bir kez açılabilir."
              >
                {DONEM_ADLARI.map((ad) => (
                  <MenuItem
                    key={ad}
                    value={ad}
                    // Zaten acilmis ad tekrar secilemez (duzenlemede kendi adi haric).
                    disabled={kullanilanAdlar.has(ad) && ad !== form?.ad}
                  >
                    {ad}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Başlangıç tarihi"
                type="date"
                value={form?.baslangicTarihi ?? ""}
                onChange={(e) =>
                  setForm((f) => (f ? { ...f, baslangicTarihi: e.target.value } : f))
                }
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
              <TextField
                label="Bitiş tarihi"
                type="date"
                value={form?.bitisTarihi ?? ""}
                onChange={(e) => setForm((f) => (f ? { ...f, bitisTarihi: e.target.value } : f))}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setForm(null)}>Vazgeç</Button>
            <IslemButonu type="submit" variant="contained" calisiyor={kaydet.isPending}>
              Kaydet
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      <OnayDialog
        acik={silinecek !== null}
        baslik="Dönem silinsin mi?"
        metin={`"${silinecek?.ad}" silinecek.`}
        onayMetni="Sil"
        calisiyor={sil.isPending}
        onOnay={() => silinecek && sil.mutate(silinecek.hashId)}
        onKapat={() => setSilinecek(null)}
      />
    </>
  );
}
