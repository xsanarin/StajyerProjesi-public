import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  LinearProgress,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import CheckIcon from "@mui/icons-material/Check";
import CloseIcon from "@mui/icons-material/Close";
import AddIcon from "@mui/icons-material/Add";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import {
  maddeOnayla,
  onerilenSablonlar,
  ozelMaddeEkle,
  ozelMaddeSil,
  sablonAta,
  seviyeYukselt,
  stajGorevleri,
} from "../../api/sorumlu";
import { sonrakiSeviye } from "../../api/types";
import type { GorevDurumu, Seviye, StajGorevMaddeResponse, StajResponse } from "../../api/types";
import { GorevDurumEtiketi } from "../../ui/GorevDurumEtiketi";
import { IslemButonu } from "../../ui/IslemButonu";
import { useToast } from "../../ui/ToastProvider";

export function GorevSekmesi({ staj }: { staj: StajResponse }) {
  const toast = useToast();
  const qc = useQueryClient();
  const stajHashId = staj.hashId;

  const gorevler = useQuery({
    queryKey: ["sorumlu", "gorevler", stajHashId],
    queryFn: () => stajGorevleri(stajHashId),
  });
  const sablonlar = useQuery({
    queryKey: ["sorumlu", "onerilen-sablonlar", stajHashId],
    queryFn: () => onerilenSablonlar(stajHashId),
  });

  const [atanacakSablon, setAtanacakSablon] = useState("");
  // Sablonda olmayan madde ekleme: hangi goreve, ne metinle.
  const [ozelMadde, setOzelMadde] = useState<{ gorevHashId: string; metin: string } | null>(null);
  // Silme onayi: yanlislikla tiklanip is kaybi olmasin.
  const [silinecek, setSilinecek] = useState<StajGorevMaddeResponse | null>(null);
  // Red icin not zorunlu, o yuzden onay/red ayri bir dialogdan geciyor.
  const [karar, setKarar] = useState<{
    madde: StajGorevMaddeResponse;
    durum: GorevDurumu;
    not: string;
  } | null>(null);

  function tazele() {
    qc.invalidateQueries({ queryKey: ["sorumlu", "gorevler", stajHashId] });
  }

  const ata = useMutation({
    mutationFn: (sablonHashId: string) => sablonAta(stajHashId, sablonHashId),
    onSuccess: () => {
      tazele();
      setAtanacakSablon("");
      toast.basari("Şablon atandı.");
    },
    onError: (err) => toast.hata(err, "Şablon atanamadı"),
  });

  const onayla = useMutation({
    mutationFn: (d: { maddeHashId: string; durum: GorevDurumu; not: string }) =>
      maddeOnayla(d.maddeHashId, { durum: d.durum, onayNotu: d.not || null }),
    onSuccess: () => {
      tazele();
      setKarar(null);
      toast.basari("Karar kaydedildi.");
    },
    onError: (err) => toast.hata(err, "Karar kaydedilemedi"),
  });

  const maddeEkle = useMutation({
    mutationFn: (d: { gorevHashId: string; metin: string }) =>
      ozelMaddeEkle(d.gorevHashId, d.metin.trim()),
    onSuccess: () => {
      tazele();
      setOzelMadde(null);
      toast.basari("Madde eklendi.");
    },
    onError: (err) => toast.hata(err, "Madde eklenemedi"),
  });

  const maddeSil = useMutation({
    mutationFn: (maddeHashId: string) => ozelMaddeSil(maddeHashId),
    onSuccess: () => {
      tazele();
      setSilinecek(null);
      toast.basari("Madde silindi.");
    },
    onError: (err) => toast.hata(err, "Madde silinemedi"),
  });

  const seviye = useMutation({
    mutationFn: (yeni: Seviye) => seviyeYukselt(stajHashId, yeni),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["sorumlu", "stajlar"] });
      tazele();
      toast.basari("Seviye yükseltildi.");
    },
    onError: (err) => toast.hata(err, "Seviye yükseltilemedi"),
  });

  /** Son seviyedeyse null — o zaman yukseltme butonu hic gosterilmez. */
  const yukseltilecek = sonrakiSeviye(staj.seviye);

  // Kapanmis stajda yazma islemleri backend'de zaten reddediliyor; butonlari da
  // kapatalim ki kullanici bosuna deneyip hata almasin (me/GorevlerimSekmesi ile ayni desen).
  const saltOkunur = staj.durum !== "DEVAM_EDIYOR";

  const redNotuEksik = karar?.durum === "REDDEDILDI" && karar.not.trim() === "";

  return (
    <>
      {saltOkunur && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Bu staj {staj.durum} durumunda; görevler yalnızca görüntülenebilir.
        </Alert>
      )}

      <Paper sx={{ p: 2, mb: 2 }}>
        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems={{ md: "center" }}>
          <TextField
            select
            size="small"
            label="Önerilen şablon"
            value={atanacakSablon}
            onChange={(e) => setAtanacakSablon(e.target.value)}
            disabled={saltOkunur}
            sx={{ minWidth: 260 }}
            helperText={
              sablonlar.data?.length === 0
                ? "Bu departman ve seviye için aktif şablon yok."
                : "Şablon staja kopyalanır; sonraki şablon değişiklikleri bu görevi etkilemez."
            }
          >
            {sablonlar.data?.map((s) => (
              <MenuItem key={s.hashId} value={s.hashId}>
                {s.ad} ({s.maddeler.length} madde)
              </MenuItem>
            ))}
          </TextField>
          <IslemButonu
            variant="contained"
            calisiyor={ata.isPending}
            disabled={!atanacakSablon || saltOkunur}
            onClick={() => ata.mutate(atanacakSablon)}
          >
            Ata
          </IslemButonu>

          <Box sx={{ flexGrow: 1 }} />

          <Stack direction="row" spacing={1} alignItems="center">
            <Typography variant="body2">Seviye: {staj.seviye ?? "-"}</Typography>
            {/* Buton bir sonraki seviyeye yukseltir; son seviyede hic cikmaz.
                Eskiden "LEVEL_1 ise LEVEL_2'ye" diye sabitti, LEVEL_5'e
                cikinca aradaki seviyelerde buton kayboluyordu. */}
            {yukseltilecek && (
              <IslemButonu
                size="small"
                variant="outlined"
                calisiyor={seviye.isPending}
                disabled={saltOkunur}
                onClick={() => seviye.mutate(yukseltilecek)}
              >
                {yukseltilecek}'ye Yükselt
              </IslemButonu>
            )}
          </Stack>
        </Stack>
      </Paper>

      {gorevler.isPending && <Typography>Yükleniyor...</Typography>}
      {gorevler.data?.length === 0 && (
        <Alert severity="info">Bu staja henüz görev atanmamış.</Alert>
      )}

      {gorevler.data?.map((g) => (
        <Accordion key={g.hashId} defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ width: "100%", pr: 2 }}>
              <Typography sx={{ flexGrow: 1 }}>{g.baslik}</Typography>
              {g.seviye && <Chip size="small" label={g.seviye} variant="outlined" />}
              <GorevDurumEtiketi durum={g.durum} />
              {g.ilerleme && (
                <Typography variant="body2" color="text.secondary">
                  {g.ilerleme.tamamlanan}/{g.ilerleme.toplam} (%{g.ilerleme.yuzde})
                </Typography>
              )}
            </Stack>
          </AccordionSummary>
          <AccordionDetails>
            {g.ilerleme && (
              <LinearProgress
                variant="determinate"
                value={g.ilerleme.yuzde}
                sx={{ mb: 2, height: 8, borderRadius: 4 }}
              />
            )}
            {g.aciklama && (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {g.aciklama}
              </Typography>
            )}

            <Stack spacing={1}>
              {g.maddeler.map((m) => (
                <Paper key={m.hashId} variant="outlined" sx={{ p: 1.5 }}>
                  <Stack
                    direction={{ xs: "column", sm: "row" }}
                    spacing={1.5}
                    alignItems={{ xs: "stretch", sm: "center" }}
                  >
                    <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                      <Stack direction="row" spacing={1} alignItems="center">
                        <Typography variant="body2">{m.maddeMetni}</Typography>
                        {/* Sablondan gelmeyen madde isaretlenir: sorumlu hangi isi
                            kendisinin ekledigini bir bakista gorsun. */}
                        {m.ozelMi && (
                          <Chip size="small" label="Özel" color="info" variant="outlined" />
                        )}
                      </Stack>
                      {m.stajyerNotu && (
                        <Typography variant="caption" color="text.secondary" display="block">
                          Stajyer notu: {m.stajyerNotu}
                        </Typography>
                      )}
                      {m.onayNotu && (
                        <Typography variant="caption" color="text.secondary" display="block">
                          Geri bildirim: {m.onayNotu}
                        </Typography>
                      )}
                    </Box>
                    <Stack direction="row" spacing={1} alignItems="center" sx={{ flexShrink: 0 }}>
                      <GorevDurumEtiketi durum={m.durum} />
                      {/* Onay karari yalnizca stajyer maddeyi tamamladiktan sonra ve
                          staj acikken anlamli. */}
                      {m.durum === "TAMAMLANDI" && !saltOkunur && (
                        <>
                          <Button
                            size="small"
                            variant="outlined"
                            color="success"
                            startIcon={<CheckIcon />}
                            onClick={() => setKarar({ madde: m, durum: "ONAYLANDI", not: "" })}
                          >
                            Onayla
                          </Button>
                          <Button
                            size="small"
                            variant="outlined"
                            color="warning"
                            startIcon={<CloseIcon />}
                            onClick={() => setKarar({ madde: m, durum: "REDDEDILDI", not: "" })}
                          >
                            Reddet
                          </Button>
                        </>
                      )}
                      {/* Silme yalnizca ozel maddede: sablondan gelen madde snapshot'in
                          parcasi. Onaylanmis madde de is kaydi, backend ikisini de reddeder. */}
                      {m.ozelMi && m.durum !== "ONAYLANDI" && !saltOkunur && (
                        <Tooltip title="Özel maddeyi sil">
                          <IconButton size="small" color="error" onClick={() => setSilinecek(m)}>
                            <DeleteOutlineIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      )}
                    </Stack>
                  </Stack>
                </Paper>
              ))}
            </Stack>

            {/* Sablon her stajyere birebir uymuyor; bu staja ozgu is buradan eklenir.
                Sablonun kendisi degismez, ekleme yalnizca bu stajin kopyasina yazilir. */}
            {!saltOkunur && (
              <Button
                size="small"
                startIcon={<AddIcon />}
                sx={{ mt: 1.5 }}
                onClick={() => setOzelMadde({ gorevHashId: g.hashId, metin: "" })}
              >
                Madde Ekle
              </Button>
            )}
          </AccordionDetails>
        </Accordion>
      ))}

      {/* ---- Sablonda olmayan madde ---- */}
      <Dialog open={ozelMadde !== null} onClose={() => setOzelMadde(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Şablonda Olmayan Madde Ekle</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Madde yalnızca bu stajyerin görevine eklenir; şablon değişmez, diğer
            stajyerler etkilenmez. Görev tamamlanmışsa yeni madde onu tekrar açar.
          </Typography>
          <TextField
            label="Madde metni"
            value={ozelMadde?.metin ?? ""}
            onChange={(e) => setOzelMadde((o) => (o ? { ...o, metin: e.target.value } : o))}
            multiline
            minRows={2}
            fullWidth
            autoFocus
            inputProps={{ maxLength: 2000 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOzelMadde(null)}>Vazgeç</Button>
          <IslemButonu
            variant="contained"
            calisiyor={maddeEkle.isPending}
            disabled={(ozelMadde?.metin ?? "").trim() === ""}
            onClick={() => ozelMadde && maddeEkle.mutate(ozelMadde)}
          >
            Ekle
          </IslemButonu>
        </DialogActions>
      </Dialog>

      {/* ---- Ozel madde silme ---- */}
      <Dialog open={silinecek !== null} onClose={() => setSilinecek(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Maddeyi Sil</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 1 }}>
            {silinecek?.maddeMetni}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Bu madde görevden tamamen kaldırılır, geri alınamaz. Kalan maddelerin
            hepsi bitmişse görev tekrar tamamlanmış sayılır.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSilinecek(null)}>Vazgeç</Button>
          <IslemButonu
            variant="contained"
            color="error"
            calisiyor={maddeSil.isPending}
            onClick={() => silinecek && maddeSil.mutate(silinecek.hashId)}
          >
            Sil
          </IslemButonu>
        </DialogActions>
      </Dialog>

      {/* ---- Onay / red ---- */}
      <Dialog open={karar !== null} onClose={() => setKarar(null)} maxWidth="sm" fullWidth>
        <DialogTitle>{karar?.durum === "ONAYLANDI" ? "Maddeyi Onayla" : "Maddeyi Reddet"}</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {karar?.madde.maddeMetni}
          </Typography>
          <TextField
            label={karar?.durum === "REDDEDILDI" ? "Geri bildirim (zorunlu)" : "Not (isteğe bağlı)"}
            value={karar?.not ?? ""}
            onChange={(e) => setKarar((k) => (k ? { ...k, not: e.target.value } : k))}
            multiline
            minRows={2}
            fullWidth
            error={redNotuEksik}
            helperText={
              redNotuEksik ? "Reddederken stajyerin neyi düzelteceğini yazmanız gerekiyor." : " "
            }
            inputProps={{ maxLength: 2000 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setKarar(null)}>Vazgeç</Button>
          <IslemButonu
            variant="contained"
            color={karar?.durum === "ONAYLANDI" ? "success" : "warning"}
            calisiyor={onayla.isPending}
            disabled={redNotuEksik}
            onClick={() =>
              karar &&
              onayla.mutate({
                maddeHashId: karar.madde.hashId,
                durum: karar.durum,
                not: karar.not,
              })
            }
          >
            {karar?.durum === "ONAYLANDI" ? "Onayla" : "Reddet"}
          </IslemButonu>
        </DialogActions>
      </Dialog>
    </>
  );
}
