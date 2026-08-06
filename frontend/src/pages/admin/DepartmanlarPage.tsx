import {
  Box,
  Button,
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
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import {
  departmanGuncelle,
  departmanKaydet,
  departmanSil,
  departmanlariGetir,
} from "../../api/admin";
import type { DepartmanResponse } from "../../api/types";
import { AramaKutusu, aramaEslesiyor } from "../../ui/AramaKutusu";
import { IslemButonu } from "../../ui/IslemButonu";
import { OnayDialog } from "../../ui/OnayDialog";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";
import { useToast } from "../../ui/ToastProvider";

export function DepartmanlarPage() {
  const toast = useToast();
  const qc = useQueryClient();

  const liste = useQuery({ queryKey: ["departmanlar"], queryFn: departmanlariGetir });

  // duzenlenen: null -> dialog kapali, {hashId: null} -> yeni kayit, dolu -> guncelleme.
  const [duzenlenen, setDuzenlenen] = useState<{ hashId: string | null; ad: string } | null>(null);
  const [silinecek, setSilinecek] = useState<DepartmanResponse | null>(null);
  const [arama, setArama] = useState("");

  const gorunenler = useMemo(
    () => (liste.data ?? []).filter((d) => aramaEslesiyor([d.ad], arama)),
    [liste.data, arama],
  );
  const iskeletGoster = useGecikmeliGoster(liste.isPending);

  const kaydet = useMutation({
    mutationFn: (d: { hashId: string | null; ad: string }) =>
      d.hashId ? departmanGuncelle(d.hashId, { ad: d.ad }) : departmanKaydet({ ad: d.ad }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["departmanlar"] });
      setDuzenlenen(null);
      toast.basari("Departman kaydedildi.");
    },
    onError: (err) => toast.hata(err, "Departman kaydedilemedi"),
  });

  const sil = useMutation({
    mutationFn: (hashId: string) => departmanSil(hashId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["departmanlar"] });
      setSilinecek(null);
      toast.basari("Departman silindi.");
    },
    onError: (err) => toast.hata(err, "Departman silinemedi"),
  });

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          Departmanlar
        </Typography>
        <AramaKutusu deger={arama} onDegis={setArama} placeholder="Departman ara..." />
        <Button variant="contained" onClick={() => setDuzenlenen({ hashId: null, ad: "" })}>
          Yeni Departman
        </Button>
      </Stack>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Ad</TableCell>
              <TableCell align="right" width={56} />
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={2} />}
            {liste.isError && (
              <TableRow>
                <TableCell colSpan={2}>Liste alınamadı.</TableCell>
              </TableRow>
            )}
            {gorunenler.map((d) => (
              <TableRow key={d.hashId} hover>
                <TableCell>{d.ad}</TableCell>
                <TableCell align="right">
                  <SatirMenusu
                    islemler={[
                      {
                        etiket: "Düzenle",
                        onSecim: () => setDuzenlenen({ hashId: d.hashId, ad: d.ad }),
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

      <Dialog open={duzenlenen !== null} onClose={() => setDuzenlenen(null)} maxWidth="xs" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (duzenlenen) {
              kaydet.mutate(duzenlenen);
            }
          }}
        >
          <DialogTitle>{duzenlenen?.hashId ? "Departmanı Düzenle" : "Yeni Departman"}</DialogTitle>
          <DialogContent>
            <TextField
              label="Departman adı"
              value={duzenlenen?.ad ?? ""}
              onChange={(e) =>
                setDuzenlenen((d) => (d ? { ...d, ad: e.target.value } : d))
              }
              required
              autoFocus
              fullWidth
              sx={{ mt: 1 }}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDuzenlenen(null)}>Vazgeç</Button>
            <IslemButonu type="submit" variant="contained" calisiyor={kaydet.isPending}>
              Kaydet
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      <OnayDialog
        acik={silinecek !== null}
        baslik="Departman silinsin mi?"
        metin={`"${silinecek?.ad}" silinecek. Departmana bağlı staj varsa backend işlemi reddedebilir.`}
        onayMetni="Sil"
        calisiyor={sil.isPending}
        onOnay={() => silinecek && sil.mutate(silinecek.hashId)}
        onKapat={() => setSilinecek(null)}
      />
    </>
  );
}
