import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControlLabel,
  IconButton,
  MenuItem,
  Paper,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import {
  departmanlariGetir,
  sablonGuncelle,
  sablonOlustur,
  sablonPasiflestir,
  sablonlariGetir,
} from "../../api/admin";
import { SEVIYELER } from "../../api/types";
import type { SablonResponse, Seviye } from "../../api/types";
import { AramaKutusu, aramaEslesiyor } from "../../ui/AramaKutusu";
import { IslemButonu } from "../../ui/IslemButonu";
import { OnayDialog } from "../../ui/OnayDialog";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";
import { useToast } from "../../ui/ToastProvider";

/**
 * Gorev sablonlari. Sorumlunun "onerilen sablonlar" listesi buradan beslenir:
 * bir sablon ancak AKTIF ise ve stajin departmani + seviyesi ile eslesiyorsa onerilir.
 * Bu ekran olmadan hicbir staja gorev atanamaz.
 *
 * Liste HER ZAMAN tek departmana ait: ustteki secim ne ise o. Karisik departman
 * listesi gosterilmez, filtreyi backend yapar (bkz. sablonlariGetir).
 */

type Form = {
  hashId: string | null;
  ad: string;
  aciklama: string;
  departmanHashId: string;
  seviye: Seviye;
  maddeler: string[];
};

const bosForm: Form = {
  hashId: null,
  ad: "",
  aciklama: "",
  departmanHashId: "",
  seviye: "LEVEL_1",
  maddeler: [""],
};

export function SablonlarPage() {
  const toast = useToast();
  const qc = useQueryClient();

  const [sadeceAktif, setSadeceAktif] = useState(false);
  const [secilenDepartman, setSecilenDepartman] = useState("");
  const [form, setForm] = useState<Form | null>(null);
  const [pasiflenecek, setPasiflenecek] = useState<SablonResponse | null>(null);
  const [arama, setArama] = useState("");

  const departmanlar = useQuery({ queryKey: ["departmanlar"], queryFn: departmanlariGetir });

  // Departman secilmeden liste anlamsiz; ilki otomatik secilir ki ekran bos acilmasin.
  useEffect(() => {
    if (!secilenDepartman && departmanlar.data?.length) {
      setSecilenDepartman(departmanlar.data[0].hashId);
    }
  }, [departmanlar.data, secilenDepartman]);

  const liste = useQuery({
    queryKey: ["sablonlar", sadeceAktif, secilenDepartman],
    queryFn: () => sablonlariGetir(sadeceAktif, secilenDepartman),
    enabled: secilenDepartman !== "",
  });

  // Departman ve "sadece aktif" backend'de filtreleniyor; arama onlarin uzerine biniyor.
  const gorunenler = useMemo(
    () => (liste.data ?? []).filter((s) => aramaEslesiyor([s.ad, s.aciklama, s.seviye], arama)),
    [liste.data, arama],
  );

  function tazele() {
    qc.invalidateQueries({ queryKey: ["sablonlar"] });
    // Sorumlu ekranindaki oneri listesi de bayatlar.
    qc.invalidateQueries({ queryKey: ["sorumlu", "onerilen-sablonlar"] });
  }

  const kaydet = useMutation({
    mutationFn: (f: Form) => {
      const govde = {
        ad: f.ad,
        aciklama: f.aciklama || null,
        departmanHashId: f.departmanHashId,
        seviye: f.seviye,
        // Bos satirlar formda kolaylik olsun diye duruyor, backend'e gitmemeli.
        // Sira 1'den baslar ve ekrandaki siralamayi izler.
        maddeler: f.maddeler
          .map((m) => m.trim())
          .filter((m) => m !== "")
          .map((maddeMetni, i) => ({ maddeMetni, sira: i + 1 })),
      };
      return f.hashId ? sablonGuncelle(f.hashId, govde) : sablonOlustur(govde);
    },
    onSuccess: () => {
      tazele();
      setForm(null);
      toast.basari("Şablon kaydedildi.");
    },
    onError: (err) => toast.hata(err, "Şablon kaydedilemedi"),
  });

  const pasiflestir = useMutation({
    mutationFn: sablonPasiflestir,
    onSuccess: () => {
      tazele();
      setPasiflenecek(null);
      toast.basari("Şablon pasifleştirildi.");
    },
    onError: (err) => toast.hata(err, "Şablon pasifleştirilemedi"),
  });

  // Departman secilmeden sorgu calismiyor; o durumda isPending'e bakip iskelet gostermeyelim.
  const iskeletGoster = useGecikmeliGoster(liste.isPending && secilenDepartman !== "");

  function maddeDegistir(i: number, deger: string) {
    setForm((f) =>
      f ? { ...f, maddeler: f.maddeler.map((m, j) => (j === i ? deger : m)) } : f,
    );
  }

  function maddeSil(i: number) {
    setForm((f) => (f ? { ...f, maddeler: f.maddeler.filter((_, j) => j !== i) } : f));
  }

  function maddeTasi(i: number, yon: -1 | 1) {
    setForm((f) => {
      if (!f) {
        return f;
      }
      const hedef = i + yon;
      if (hedef < 0 || hedef >= f.maddeler.length) {
        return f;
      }
      const yeni = [...f.maddeler];
      [yeni[i], yeni[hedef]] = [yeni[hedef], yeni[i]];
      return { ...f, maddeler: yeni };
    });
  }

  const doluMaddeSayisi = form?.maddeler.filter((m) => m.trim() !== "").length ?? 0;

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          Görev Şablonları
        </Typography>
        <TextField
          select
          size="small"
          label="Departman"
          value={secilenDepartman}
          onChange={(e) => setSecilenDepartman(e.target.value)}
          sx={{
            minWidth: 200,
            // Etiket varsayilan 14px iceriden basliyor; 6px sola aldik. Cerceve
            // centigi de ayni kadar kaymali, yoksa yazi kenarligin ustune biner.
            "& .MuiInputLabel-shrink": { transform: "translate(8px, -9px) scale(0.75)" },
            "& .MuiOutlinedInput-notchedOutline": { pl: "2px" },
          }}
        >
          {departmanlar.data?.map((d) => (
            <MenuItem key={d.hashId} value={d.hashId}>
              {d.ad}
            </MenuItem>
          ))}
        </TextField>
        <AramaKutusu deger={arama} onDegis={setArama} placeholder="Şablon adı, açıklama..." />
        <FormControlLabel
          control={
            <Switch
              size="small"
              checked={sadeceAktif}
              onChange={(e) => setSadeceAktif(e.target.checked)}
            />
          }
          label="Sadece aktif"
          sx={{ mr: 2 }}
        />
        <Button
          variant="contained"
          onClick={() => setForm({ ...bosForm, departmanHashId: secilenDepartman })}
          disabled={secilenDepartman === ""}
        >
          Yeni Şablon
        </Button>
      </Stack>

      {departmanlar.data?.length === 0 && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          Tanımlı departman yok. Şablon oluşturabilmek için önce Departmanlar ekranından bir
          departman ekleyin.
        </Alert>
      )}

      <Alert severity="info" sx={{ mb: 2 }}>
        Liste yalnızca yukarıda seçili departmanın şablonlarını gösterir. Sorumluya önerilen
        şablonlar buradan gelir: şablon aktif olmalı ve stajın departmanı ile seviyesine
        uymalıdır. Şablon atandığında maddeler staja kopyalanır; sonradan yaptığınız
        değişiklikler eski atamaları etkilemez.
      </Alert>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Ad</TableCell>
              <TableCell>Seviye</TableCell>
              <TableCell>Madde</TableCell>
              <TableCell>Durum</TableCell>
              <TableCell align="right" width={56} />
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={5} />}
            {gorunenler.map((s) => (
              <TableRow key={s.hashId} hover>
                <TableCell>
                  {s.ad}
                  {s.aciklama && (
                    <Typography variant="caption" color="text.secondary" display="block">
                      {s.aciklama}
                    </Typography>
                  )}
                </TableCell>
                <TableCell>{s.seviye}</TableCell>
                <TableCell>{s.maddeler.length}</TableCell>
                <TableCell>
                  <Chip
                    size="small"
                    label={s.aktif ? "Aktif" : "Pasif"}
                    color={s.aktif ? "success" : "default"}
                  />
                </TableCell>
                <TableCell align="right">
                  <SatirMenusu
                    islemler={[
                      {
                        etiket: "Düzenle",
                        onSecim: () =>
                          setForm({
                            hashId: s.hashId,
                            ad: s.ad,
                            aciklama: s.aciklama ?? "",
                            departmanHashId: s.departmanHashId,
                            seviye: s.seviye,
                            maddeler: s.maddeler.length
                              ? [...s.maddeler]
                                  .sort((a, b) => (a.sira ?? 0) - (b.sira ?? 0))
                                  .map((m) => m.maddeMetni)
                              : [""],
                          }),
                      },
                      {
                        etiket: "Pasifleştir",
                        onSecim: () => setPasiflenecek(s),
                        tehlikeli: true,
                        gizli: !s.aktif,
                      },
                    ]}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={form !== null} onClose={() => setForm(null)} maxWidth="md" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (form && doluMaddeSayisi > 0) {
              kaydet.mutate(form);
            }
          }}
        >
          <DialogTitle>{form?.hashId ? "Şablonu Düzenle" : "Yeni Şablon"}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                label="Şablon adı"
                value={form?.ad ?? ""}
                onChange={(e) => setForm((f) => (f ? { ...f, ad: e.target.value } : f))}
                required
                autoFocus
                fullWidth
              />
              <TextField
                label="Açıklama"
                value={form?.aciklama ?? ""}
                onChange={(e) => setForm((f) => (f ? { ...f, aciklama: e.target.value } : f))}
                multiline
                minRows={2}
                fullWidth
              />
              <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                <TextField
                  select
                  label="Departman"
                  value={form?.departmanHashId ?? ""}
                  onChange={(e) =>
                    setForm((f) => (f ? { ...f, departmanHashId: e.target.value } : f))
                  }
                  required
                  fullWidth
                >
                  {departmanlar.data?.map((d) => (
                    <MenuItem key={d.hashId} value={d.hashId}>
                      {d.ad}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  select
                  label="Seviye"
                  value={form?.seviye ?? "LEVEL_1"}
                  onChange={(e) =>
                    setForm((f) => (f ? { ...f, seviye: e.target.value as Seviye } : f))
                  }
                  required
                  fullWidth
                >
                  {SEVIYELER.map((sv) => (
                    <MenuItem key={sv} value={sv}>
                      {sv}
                    </MenuItem>
                  ))}
                </TextField>
              </Stack>

              <Divider />
              <Typography variant="subtitle2">
                Maddeler ({doluMaddeSayisi})
              </Typography>

              {form?.maddeler.map((m, i) => (
                <Stack key={i} direction="row" spacing={1} alignItems="flex-start">
                  <TextField
                    label={`${i + 1}. madde`}
                    value={m}
                    onChange={(e) => maddeDegistir(i, e.target.value)}
                    multiline
                    fullWidth
                    inputProps={{ maxLength: 2000 }}
                  />
                  <IconButton
                    onClick={() => maddeTasi(i, -1)}
                    disabled={i === 0}
                    aria-label="Yukarı taşı"
                  >
                    <ArrowUpwardIcon fontSize="small" />
                  </IconButton>
                  <IconButton
                    onClick={() => maddeTasi(i, 1)}
                    disabled={i === (form?.maddeler.length ?? 0) - 1}
                    aria-label="Aşağı taşı"
                  >
                    <ArrowDownwardIcon fontSize="small" />
                  </IconButton>
                  <IconButton
                    onClick={() => maddeSil(i)}
                    disabled={(form?.maddeler.length ?? 0) <= 1}
                    aria-label="Maddeyi sil"
                  >
                    <DeleteOutlineIcon fontSize="small" />
                  </IconButton>
                </Stack>
              ))}

              <Button
                onClick={() => setForm((f) => (f ? { ...f, maddeler: [...f.maddeler, ""] } : f))}
                sx={{ alignSelf: "flex-start" }}
              >
                + Madde Ekle
              </Button>

              {doluMaddeSayisi === 0 && (
                <Alert severity="warning">En az bir madde yazmalısınız.</Alert>
              )}
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setForm(null)}>Vazgeç</Button>
            <IslemButonu
              type="submit"
              variant="contained"
              calisiyor={kaydet.isPending}
              disabled={doluMaddeSayisi === 0}
            >
              Kaydet
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      <OnayDialog
        acik={pasiflenecek !== null}
        baslik="Şablon pasifleştirilsin mi?"
        metin={`"${pasiflenecek?.ad}" artık sorumlulara önerilmeyecek. Bu şablondan daha önce atanmış görevler olduğu gibi kalır.`}
        onayMetni="Pasifleştir"
        calisiyor={pasiflestir.isPending}
        onOnay={() => pasiflenecek && pasiflestir.mutate(pasiflenecek.hashId)}
        onKapat={() => setPasiflenecek(null)}
      />
    </>
  );
}
