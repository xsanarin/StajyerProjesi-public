import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
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
  departmanlariGetir,
  sorumluAyril,
  sorumluGuncelle,
  sorumluOlustur,
  sorumlulariGetir,
} from "../../api/admin";
import type { ProvisionResponse, SorumluResponse } from "../../api/types";
import { AramaKutusu, aramaEslesiyor } from "../../ui/AramaKutusu";
import { GeciciSifreDialog } from "../../ui/GeciciSifreDialog";
import { IslemButonu } from "../../ui/IslemButonu";
import { OnayDialog } from "../../ui/OnayDialog";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";
import { TelefonAlani, telefonGecerli } from "../../ui/TelefonAlani";
import { useToast } from "../../ui/ToastProvider";

/**
 * hashId === null -> yeni kayit, dolu -> duzenleme. DepartmanlarPage'deki
 * "duzenlenen" kalibinin aynisi; tek dialog iki isi de gorur.
 */
type SorumluForm = {
  hashId: string | null;
  ad: string;
  soyad: string;
  email: string;
  telefon: string;
  departmanHashId: string;
};

const bosSorumlu: SorumluForm = {
  hashId: null,
  ad: "",
  soyad: "",
  email: "",
  telefon: "",
  departmanHashId: "",
};

const formaDoldur = (s: SorumluResponse): SorumluForm => ({
  hashId: s.hashId,
  ad: s.ad,
  soyad: s.soyad,
  email: s.email,
  telefon: s.telefon ?? "",
  departmanHashId: s.departmanHashId ?? "",
});

export function SorumlularPage() {
  const toast = useToast();
  const qc = useQueryClient();

  const sorumlular = useQuery({ queryKey: ["sorumlular"], queryFn: sorumlulariGetir });
  const departmanlar = useQuery({ queryKey: ["departmanlar"], queryFn: departmanlariGetir });

  const [sorumluForm, setSorumluForm] = useState<SorumluForm | null>(null);
  const [ayrilacak, setAyrilacak] = useState<SorumluResponse | null>(null);
  const [provizyon, setProvizyon] = useState<ProvisionResponse | null>(null);
  const [arama, setArama] = useState("");

  const gorunenler = useMemo(
    () =>
      (sorumlular.data ?? []).filter((s) =>
        aramaEslesiyor(
          [s.ad, s.soyad, `${s.ad} ${s.soyad}`, s.email, s.telefon, s.departmanAd],
          arama,
        ),
      ),
    [sorumlular.data, arama],
  );
  const iskeletGoster = useGecikmeliGoster(sorumlular.isPending);

  const sorumluEkle = useMutation({
    mutationFn: (f: SorumluForm) =>
      sorumluOlustur({
        ad: f.ad,
        soyad: f.soyad,
        email: f.email,
        telefon: f.telefon || null,
        departmanHashId: f.departmanHashId || null,
      }),
    onSuccess: (yanit) => {
      qc.invalidateQueries({ queryKey: ["sorumlular"] });
      setSorumluForm(null);
      setProvizyon(yanit);
    },
    onError: (err) => toast.hata(err, "Sorumlu oluşturulamadı"),
  });

  const sorumluDuzenle = useMutation({
    mutationFn: (f: SorumluForm) =>
      sorumluGuncelle(f.hashId!, {
        ad: f.ad,
        soyad: f.soyad,
        email: f.email,
        telefon: f.telefon || null,
        departmanHashId: f.departmanHashId || null,
      }),
    onSuccess: () => {
      // Sorumlu adi/departmani staj listesinde de gorunuyor, orayi da tazele.
      qc.invalidateQueries({ queryKey: ["sorumlular"] });
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setSorumluForm(null);
      toast.basari("Sorumlu bilgileri güncellendi.");
    },
    onError: (err) => toast.hata(err, "Sorumlu güncellenemedi"),
  });

  const ayir = useMutation({
    mutationFn: (hashId: string) => sorumluAyril(hashId),
    onSuccess: () => {
      // Terfi mantigi stajlari da degistirir (yedek -> asil), o listeyi de tazele.
      qc.invalidateQueries({ queryKey: ["sorumlular"] });
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setAyrilacak(null);
      toast.basari("Sorumlu ayrıldı, yedekler terfi ettirildi.");
    },
    onError: (err) => toast.hata(err, "Sorumlu ayrılamadı"),
  });

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          Sorumlular
        </Typography>
        <AramaKutusu deger={arama} onDegis={setArama} placeholder="Ad, e-posta, departman..." />
        <Button variant="contained" onClick={() => setSorumluForm(bosSorumlu)}>
          Sorumlu Ekle
        </Button>
      </Stack>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Ad Soyad</TableCell>
              <TableCell>E-posta</TableCell>
              <TableCell>Telefon</TableCell>
              <TableCell>Departman</TableCell>
              <TableCell align="right" width={56} />
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={5} />}
            {gorunenler.map((s) => (
              <TableRow key={s.hashId} hover>
                <TableCell>
                  {s.ad} {s.soyad}
                </TableCell>
                <TableCell>{s.email}</TableCell>
                <TableCell>{s.telefon ?? "-"}</TableCell>
                <TableCell>{s.departmanAd ?? "-"}</TableCell>
                <TableCell align="right">
                  <SatirMenusu
                    islemler={[
                      { etiket: "Düzenle", onSecim: () => setSorumluForm(formaDoldur(s)) },
                      { etiket: "Ayrıl", onSecim: () => setAyrilacak(s), tehlikeli: true },
                    ]}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={sorumluForm !== null} onClose={() => setSorumluForm(null)} maxWidth="xs" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (!sorumluForm) return;
            if (sorumluForm.hashId) {
              sorumluDuzenle.mutate(sorumluForm);
            } else {
              sorumluEkle.mutate(sorumluForm);
            }
          }}
        >
          <DialogTitle>{sorumluForm?.hashId ? "Sorumluyu Düzenle" : "Sorumlu Ekle"}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                label="Ad"
                value={sorumluForm?.ad ?? ""}
                onChange={(e) => setSorumluForm((f) => (f ? { ...f, ad: e.target.value } : f))}
                required
                autoFocus
                fullWidth
              />
              <TextField
                label="Soyad"
                value={sorumluForm?.soyad ?? ""}
                onChange={(e) => setSorumluForm((f) => (f ? { ...f, soyad: e.target.value } : f))}
                required
                fullWidth
              />
              <TextField
                label="E-posta"
                type="email"
                value={sorumluForm?.email ?? ""}
                onChange={(e) => setSorumluForm((f) => (f ? { ...f, email: e.target.value } : f))}
                required
                fullWidth
                helperText={
                  sorumluForm?.hashId
                    ? "Giriş kimliği. Değiştirirsen kişi yeni e-postayla girer, şifresi değişmez."
                    : "Giriş bu e-posta ile yapılır."
                }
              />
              <TelefonAlani
                deger={sorumluForm?.telefon ?? ""}
                onDegis={(v) => setSorumluForm((f) => (f ? { ...f, telefon: v } : f))}
              />
              <TextField
                select
                label="Departman"
                value={sorumluForm?.departmanHashId ?? ""}
                onChange={(e) =>
                  setSorumluForm((f) => (f ? { ...f, departmanHashId: e.target.value } : f))
                }
                fullWidth
              >
                <MenuItem value="">(seçilmedi)</MenuItem>
                {departmanlar.data?.map((d) => (
                  <MenuItem key={d.hashId} value={d.hashId}>
                    {d.ad}
                  </MenuItem>
                ))}
              </TextField>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setSorumluForm(null)}>Vazgeç</Button>
            <IslemButonu
              type="submit"
              variant="contained"
              calisiyor={sorumluEkle.isPending || sorumluDuzenle.isPending}
              disabled={!telefonGecerli(sorumluForm?.telefon ?? "")}
            >
              {sorumluForm?.hashId ? "Kaydet" : "Oluştur"}
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      <GeciciSifreDialog provizyon={provizyon} onKapat={() => setProvizyon(null)} />

      <OnayDialog
        acik={ayrilacak !== null}
        baslik="Sorumlu ayrılsın mı?"
        metin={`${ayrilacak?.ad} ${ayrilacak?.soyad} pasifleştirilecek, yedek olduğu stajlarda yedekler asile terfi edecek. Yedeksiz aktif stajyeri varsa backend işlemi reddeder.`}
        onayMetni="Ayrıl"
        calisiyor={ayir.isPending}
        onOnay={() => ayrilacak && ayir.mutate(ayrilacak.hashId)}
        onKapat={() => setAyrilacak(null)}
      />
    </>
  );
}
