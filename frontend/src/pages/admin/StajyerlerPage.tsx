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
import { useEffect, useState } from "react";
import { stajyerAra, stajyerGuncelle, stajyerOlustur } from "../../api/admin";
import type { ProvisionResponse, StajyerResponse } from "../../api/types";
import { AramaKutusu } from "../../ui/AramaKutusu";
import { GeciciSifreDialog } from "../../ui/GeciciSifreDialog";
import { IslemButonu } from "../../ui/IslemButonu";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { SayfaCubugu } from "../../ui/SayfaCubugu";
import { bayatVeriSx, TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";
import { TelefonAlani, telefonGecerli } from "../../ui/TelefonAlani";
import { useToast } from "../../ui/ToastProvider";
import { useGecikmeliDeger } from "../../ui/useGecikmeliDeger";

/**
 * hashId === null -> yeni kayit, dolu -> duzenleme. DepartmanlarPage'deki
 * "duzenlenen" kalibinin aynisi; tek dialog iki isi de gorur.
 */
type StajyerForm = {
  hashId: string | null;
  ad: string;
  soyad: string;
  email: string;
  telefon: string;
  universite: string;
  bolum: string;
  sinif: string;
};

const bosStajyer: StajyerForm = {
  hashId: null,
  ad: "",
  soyad: "",
  email: "",
  telefon: "",
  universite: "",
  bolum: "",
  sinif: "",
};

const formaDoldur = (s: StajyerResponse): StajyerForm => ({
  hashId: s.hashId,
  ad: s.ad,
  soyad: s.soyad,
  email: s.email ?? "",
  telefon: s.telefon ?? "",
  universite: s.universite ?? "",
  bolum: s.bolum ?? "",
  // Form alani metin tutuyor; null -> "" yoksa input "null" gosterirdi.
  sinif: s.sinif == null ? "" : String(s.sinif),
});

export function StajyerlerPage() {
  const toast = useToast();
  const qc = useQueryClient();

  const [stajyerForm, setStajyerForm] = useState<StajyerForm | null>(null);
  const [provizyon, setProvizyon] = useState<ProvisionResponse | null>(null);
  const [arama, setArama] = useState("");
  const [sayfa, setSayfa] = useState(0);
  const [boyut, setBoyut] = useState(25);
  const gecikmeliArama = useGecikmeliDeger(arama);

  /**
   * Liste sunucudan sayfali geliyor. Anahtar ["stajyerler", ...] ile basladigi
   * icin mutasyonlardaki invalidateQueries({ queryKey: ["stajyerler"] }) hepsini
   * kapsar. Sinif aramasi da sunucuda: orada metne cevrilip karsilastiriliyor.
   */
  const stajyerler = useQuery({
    queryKey: ["stajyerler", gecikmeliArama, sayfa, boyut],
    queryFn: () => stajyerAra(gecikmeliArama, sayfa, boyut),
    placeholderData: (onceki) => onceki,
  });

  const gorunenler = stajyerler.data?.icerik ?? [];
  const toplamKayit = stajyerler.data?.toplamKayit ?? 0;

  // Yeni aramada ilk sayfaya don, yoksa kullanici bos tablo gorur.
  useEffect(() => {
    setSayfa(0);
  }, [gecikmeliArama]);

  const iskeletGoster = useGecikmeliGoster(stajyerler.isPending);

  const stajyerEkle = useMutation({
    mutationFn: (f: StajyerForm) =>
      stajyerOlustur({
        ad: f.ad,
        soyad: f.soyad,
        email: f.email,
        telefon: f.telefon || null,
        universite: f.universite || null,
        bolum: f.bolum || null,
        // Bos birakildiysa null; Number("") 0 dondurur, o yanlis olurdu.
        sinif: f.sinif === "" ? null : Number(f.sinif),
      }),
    onSuccess: (yanit) => {
      qc.invalidateQueries({ queryKey: ["stajyerler"] });
      setStajyerForm(null);
      setProvizyon(yanit);
    },
    onError: (err) => toast.hata(err, "Stajyer oluşturulamadı"),
  });

  const stajyerDuzenle = useMutation({
    mutationFn: (f: StajyerForm) =>
      stajyerGuncelle(f.hashId!, {
        ad: f.ad,
        soyad: f.soyad,
        email: f.email,
        telefon: f.telefon || null,
        universite: f.universite || null,
        bolum: f.bolum || null,
        sinif: f.sinif === "" ? null : Number(f.sinif),
      }),
    onSuccess: () => {
      // Stajyer adi staj listesinde de gorunuyor, orayi da tazele.
      qc.invalidateQueries({ queryKey: ["stajyerler"] });
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setStajyerForm(null);
      toast.basari("Stajyer bilgileri güncellendi.");
    },
    onError: (err) => toast.hata(err, "Stajyer güncellenemedi"),
  });

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          Stajyerler
        </Typography>
        <AramaKutusu deger={arama} onDegis={setArama} placeholder="Ad, e-posta, üniversite..." />
        <Button variant="contained" onClick={() => setStajyerForm(bosStajyer)}>
          Stajyer Ekle
        </Button>
      </Stack>

      <TableContainer component={Paper} sx={bayatVeriSx(stajyerler.isPlaceholderData)}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Ad Soyad</TableCell>
              <TableCell>E-posta</TableCell>
              <TableCell>Telefon</TableCell>
              <TableCell>Üniversite</TableCell>
              <TableCell>Bölüm</TableCell>
              <TableCell>Sınıf</TableCell>
              <TableCell align="right" width={56} />
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={7} />}
            {gorunenler.map((s) => (
              <TableRow key={s.hashId} hover>
                <TableCell>
                  {s.ad} {s.soyad}
                </TableCell>
                <TableCell>{s.email ?? "-"}</TableCell>
                <TableCell>{s.telefon ?? "-"}</TableCell>
                <TableCell>{s.universite ?? "-"}</TableCell>
                <TableCell>{s.bolum ?? "-"}</TableCell>
                <TableCell>{s.sinif ?? "-"}</TableCell>
                <TableCell align="right">
                  <SatirMenusu
                    islemler={[
                      { etiket: "Düzenle", onSecim: () => setStajyerForm(formaDoldur(s)) },
                    ]}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <SayfaCubugu
          toplamKayit={toplamKayit}
          sayfa={sayfa}
          boyut={boyut}
          onSayfaDegis={setSayfa}
          onBoyutDegis={(yeni) => {
            setBoyut(yeni);
            setSayfa(0);
          }}
        />
      </TableContainer>

      <Dialog open={stajyerForm !== null} onClose={() => setStajyerForm(null)} maxWidth="xs" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (!stajyerForm) return;
            if (stajyerForm.hashId) {
              stajyerDuzenle.mutate(stajyerForm);
            } else {
              stajyerEkle.mutate(stajyerForm);
            }
          }}
        >
          <DialogTitle>{stajyerForm?.hashId ? "Stajyeri Düzenle" : "Stajyer Ekle"}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                label="Ad"
                value={stajyerForm?.ad ?? ""}
                onChange={(e) => setStajyerForm((f) => (f ? { ...f, ad: e.target.value } : f))}
                required
                autoFocus
                fullWidth
              />
              <TextField
                label="Soyad"
                value={stajyerForm?.soyad ?? ""}
                onChange={(e) => setStajyerForm((f) => (f ? { ...f, soyad: e.target.value } : f))}
                required
                fullWidth
              />
              <TextField
                label="E-posta"
                type="email"
                value={stajyerForm?.email ?? ""}
                onChange={(e) => setStajyerForm((f) => (f ? { ...f, email: e.target.value } : f))}
                required
                fullWidth
                helperText={
                  stajyerForm?.hashId
                    ? "Giriş kimliği. Değiştirirsen kişi yeni e-postayla girer, şifresi değişmez."
                    : "Giriş bu e-posta ile yapılır."
                }
              />
              <TelefonAlani
                deger={stajyerForm?.telefon ?? ""}
                onDegis={(v) => setStajyerForm((f) => (f ? { ...f, telefon: v } : f))}
              />
              <TextField
                label="Üniversite"
                value={stajyerForm?.universite ?? ""}
                onChange={(e) =>
                  setStajyerForm((f) => (f ? { ...f, universite: e.target.value } : f))
                }
                fullWidth
              />
              <TextField
                label="Bölüm"
                value={stajyerForm?.bolum ?? ""}
                onChange={(e) => setStajyerForm((f) => (f ? { ...f, bolum: e.target.value } : f))}
                fullWidth
              />
              <TextField
                label="Sınıf"
                type="number"
                value={stajyerForm?.sinif ?? ""}
                onChange={(e) => setStajyerForm((f) => (f ? { ...f, sinif: e.target.value } : f))}
                inputProps={{ min: 1, max: 8 }}
                fullWidth
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setStajyerForm(null)}>Vazgeç</Button>
            <IslemButonu
              type="submit"
              variant="contained"
              calisiyor={stajyerEkle.isPending || stajyerDuzenle.isPending}
              disabled={!telefonGecerli(stajyerForm?.telefon ?? "")}
            >
              {stajyerForm?.hashId ? "Kaydet" : "Oluştur"}
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      <GeciciSifreDialog provizyon={provizyon} onKapat={() => setProvizyon(null)} />
    </>
  );
}
