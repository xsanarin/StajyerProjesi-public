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
  talepDepartmanStajyerleri,
  talepGuncelle,
  talepKaydet,
  talepSil,
  talepleriGetir,
} from "../../api/admin";
import type { TalepResponse } from "../../api/types";
import { AramaKutusu, aramaEslesiyor } from "../../ui/AramaKutusu";
import { IslemButonu } from "../../ui/IslemButonu";
import { OnayDialog } from "../../ui/OnayDialog";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";
import { useToast } from "../../ui/ToastProvider";

/** Dialog formu; hashId null ise yeni kayit. */
type TalepForm = {
  hashId: string | null;
  departmanHashId: string;
  /** Bos string = kimseye atanmamis (departman geneli). */
  stajyerHashId: string;
  baslik: string;
  aciklama: string;
};

/**
 * BT talep yonetimi (admin). Departman CRUD kalibi ile ayni.
 *
 * Talep DEPARTMANA atanir: kaydedildigi anda o departmandaki TUM sorumlularin
 * ekraninda gorunur. Ustune opsiyonel olarak o departmanda staj yapan bir
 * STAJYERE de atanabilir (V22) — talep yine ayni sorumlulara gorunur, atanan
 * kisi listede kolon olarak yazar.
 */
export function TaleplerPage() {
  const toast = useToast();
  const qc = useQueryClient();

  const liste = useQuery({ queryKey: ["admin", "talepler"], queryFn: talepleriGetir });
  // Departman listesi dialogdaki secim kutusu icin; silinmis departmanlar zaten gelmiyor.
  const departmanlar = useQuery({ queryKey: ["departmanlar"], queryFn: departmanlariGetir });

  const [duzenlenen, setDuzenlenen] = useState<TalepForm | null>(null);
  const [silinecek, setSilinecek] = useState<TalepResponse | null>(null);
  const [arama, setArama] = useState("");

  /**
   * Secilen departmanin stajyerleri. Departman degisince sorgu anahtari degisir
   * ve liste kendiliginden yenilenir; departman secilmeden istek atilmaz
   * (enabled), cunku uc departmanHashId zorunlu istiyor.
   */
  const secilenDepartman = duzenlenen?.departmanHashId ?? "";
  const stajyerler = useQuery({
    queryKey: ["admin", "talep-stajyerleri", secilenDepartman],
    queryFn: () => talepDepartmanStajyerleri(secilenDepartman),
    enabled: secilenDepartman !== "",
  });

  const gorunenler = useMemo(
    () =>
      (liste.data ?? []).filter((t) =>
        aramaEslesiyor(
          [t.baslik, t.aciklama ?? "", t.departmanAd, t.stajyerAdSoyad ?? ""],
          arama,
        ),
      ),
    [liste.data, arama],
  );
  const iskeletGoster = useGecikmeliGoster(liste.isPending);

  const kaydet = useMutation({
    mutationFn: (f: TalepForm) => {
      const govde = {
        departmanHashId: f.departmanHashId,
        // Bos secim null gider: backend'de "atama yok" demek, bos string degil.
        stajyerHashId: f.stajyerHashId || null,
        baslik: f.baslik,
        aciklama: f.aciklama || null,
      };
      return f.hashId ? talepGuncelle(f.hashId, govde) : talepKaydet(govde);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin", "talepler"] });
      setDuzenlenen(null);
      toast.basari("BT talebi kaydedildi.");
    },
    onError: (err) => toast.hata(err, "BT talebi kaydedilemedi"),
  });

  const sil = useMutation({
    mutationFn: (hashId: string) => talepSil(hashId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin", "talepler"] });
      setSilinecek(null);
      toast.basari("BT talebi silindi.");
    },
    onError: (err) => toast.hata(err, "BT talebi silinemedi"),
  });

  function yeniTalep() {
    setDuzenlenen({
      hashId: null,
      departmanHashId: "",
      stajyerHashId: "",
      baslik: "",
      aciklama: "",
    });
  }

  /**
   * Departman degisince stajyer secimi SIFIRLANIR: eski departmanin stajyeri
   * yeni departmanda gecerli degil, backend zaten reddederdi. Kullanici hatayi
   * kaydete basinca gormek yerine alani bos gorur.
   */
  function departmanSec(departmanHashId: string) {
    setDuzenlenen((d) => (d ? { ...d, departmanHashId, stajyerHashId: "" } : d));
  }

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          BT Talepleri
        </Typography>
        <AramaKutusu deger={arama} onDegis={setArama} placeholder="Talep ara..." />
        <Button variant="contained" onClick={yeniTalep}>
          Yeni Talep
        </Button>
      </Stack>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Başlık</TableCell>
              <TableCell>Departman</TableCell>
              <TableCell>Stajyer</TableCell>
              <TableCell>Açıklama</TableCell>
              <TableCell width={150}>Oluşturulma</TableCell>
              <TableCell align="right" width={56} />
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={6} />}
            {liste.isError && (
              <TableRow>
                <TableCell colSpan={6}>Liste alınamadı.</TableCell>
              </TableRow>
            )}
            {gorunenler.map((t) => (
              <TableRow key={t.hashId} hover>
                <TableCell>{t.baslik}</TableCell>
                <TableCell>{t.departmanAd}</TableCell>
                {/* Atanmamis talep = departman geneli; bos hucre "veri gelmedi" gibi
                    okunmasin diye acikca "-" yaziyoruz (diger kolonlarla ayni kalip). */}
                <TableCell>{t.stajyerAdSoyad ?? "-"}</TableCell>
                <TableCell sx={{ whiteSpace: "pre-wrap" }}>{t.aciklama ?? "-"}</TableCell>
                <TableCell>{t.olusturmaTarihi}</TableCell>
                <TableCell align="right">
                  <SatirMenusu
                    islemler={[
                      {
                        etiket: "Düzenle",
                        onSecim: () =>
                          setDuzenlenen({
                            hashId: t.hashId,
                            departmanHashId: t.departmanHashId,
                            stajyerHashId: t.stajyerHashId ?? "",
                            baslik: t.baslik,
                            aciklama: t.aciklama ?? "",
                          }),
                      },
                      { etiket: "Sil", onSecim: () => setSilinecek(t), tehlikeli: true },
                    ]}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={duzenlenen !== null} onClose={() => setDuzenlenen(null)} maxWidth="sm" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (duzenlenen) {
              kaydet.mutate(duzenlenen);
            }
          }}
        >
          <DialogTitle>{duzenlenen?.hashId ? "BT Talebini Düzenle" : "Yeni BT Talebi"}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                select
                label="Departman"
                value={duzenlenen?.departmanHashId ?? ""}
                onChange={(e) => departmanSec(e.target.value)}
                required
                fullWidth
                helperText="Talep bu departmandaki tüm sorumluların ekranında görünür."
              >
                {departmanlar.data?.map((d) => (
                  <MenuItem key={d.hashId} value={d.hashId}>
                    {d.ad}
                  </MenuItem>
                ))}
              </TextField>

              {/* Stajyer OPSIYONEL: departman geneline acilan talepler (ortak
                  ekipman, oda duzeni vb.) kimseye ait degil. Departman secilmeden
                  liste bilinemedigi icin alan o zamana kadar kapali. */}
              <TextField
                select
                label="Stajyer"
                value={duzenlenen?.stajyerHashId ?? ""}
                onChange={(e) =>
                  setDuzenlenen((d) => (d ? { ...d, stajyerHashId: e.target.value } : d))
                }
                disabled={!secilenDepartman || stajyerler.isPending}
                fullWidth
                helperText={
                  !secilenDepartman
                    ? "Önce departman seçin."
                    : stajyerler.isError
                      ? "Stajyer listesi alınamadı."
                      : stajyerler.data?.length === 0
                        ? "Bu departmanda stajı devam eden stajyer yok."
                        : "İsteğe bağlı: talep belirli bir stajyer için açılıyorsa seçin."
                }
              >
                {/* Bos secenek olmadan secim geri alinamazdi: atanmis bir talebi
                    yeniden departman geneline cevirmenin tek yolu bu satir. */}
                <MenuItem value="">
                  <em>Atanmasın (departman geneli)</em>
                </MenuItem>
                {stajyerler.data?.map((s) => (
                  <MenuItem key={s.hashId} value={s.hashId}>
                    {s.ad} {s.soyad}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                label="Başlık"
                value={duzenlenen?.baslik ?? ""}
                onChange={(e) => setDuzenlenen((d) => (d ? { ...d, baslik: e.target.value } : d))}
                required
                fullWidth
                inputProps={{ maxLength: 200 }}
              />
              <TextField
                label="Açıklama"
                value={duzenlenen?.aciklama ?? ""}
                onChange={(e) => setDuzenlenen((d) => (d ? { ...d, aciklama: e.target.value } : d))}
                multiline
                minRows={3}
                fullWidth
                inputProps={{ maxLength: 4000 }}
              />
            </Stack>
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
        baslik="BT talebi silinsin mi?"
        metin={`"${silinecek?.baslik}" silinecek ve sorumluların ekranından kalkacak.`}
        onayMetni="Sil"
        calisiyor={sil.isPending}
        onOnay={() => silinecek && sil.mutate(silinecek.hashId)}
        onKapat={() => setSilinecek(null)}
      />
    </>
  );
}
