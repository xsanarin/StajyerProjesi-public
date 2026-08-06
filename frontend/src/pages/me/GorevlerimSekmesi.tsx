import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  LinearProgress,
  Paper,
  Stack,
  TextField,
  Typography,

} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import EditNoteIcon from "@mui/icons-material/EditNote";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { gorevlerim, maddeTamamla } from "../../api/me";
import type { StajGorevMaddeResponse, StajResponse } from "../../api/types";
import { GorevDurumEtiketi } from "../../ui/GorevDurumEtiketi";
import { IslemButonu } from "../../ui/IslemButonu";
import { useToast } from "../../ui/ToastProvider";

export function GorevlerimSekmesi({ staj }: { staj: StajResponse }) {
  const toast = useToast();
  const qc = useQueryClient();

  const gorevler = useQuery({
    queryKey: ["me", "gorevler", staj.hashId],
    queryFn: () => gorevlerim(staj.hashId),
  });

  const [notDialog, setNotDialog] = useState<{ madde: StajGorevMaddeResponse; not: string } | null>(
    null,
  );

  // Tamamlanmis staj salt-okunur (kural backend'de); butonlari da kapatalim ki
  // kullanici bosuna deneyip hata almasin.
  const saltOkunur = staj.durum !== "DEVAM_EDIYOR";

  const guncelle = useMutation({
    mutationFn: (d: { maddeHashId: string; tamamlandi?: boolean; not?: string | null }) =>
      maddeTamamla(d.maddeHashId, { tamamlandi: d.tamamlandi, stajyerNotu: d.not }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["me", "gorevler", staj.hashId] });
      setNotDialog(null);
    },
    onError: (err) => toast.hata(err, "Madde güncellenemedi"),
  });

  if (gorevler.isPending) {
    return <Typography>Yükleniyor...</Typography>;
  }

  if (gorevler.data?.length === 0) {
    return <Alert severity="info">Bu staj için henüz görev atanmamış.</Alert>;
  }

  return (
    <>
      {saltOkunur && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Bu staj {staj.durum} durumunda; görevler yalnızca görüntülenebilir.
        </Alert>
      )}

      {gorevler.data?.map((g) => (
        <Accordion key={g.hashId} defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ width: "100%", pr: 2 }}>
              <Typography sx={{ flexGrow: 1 }}>{g.baslik}</Typography>
              <GorevDurumEtiketi durum={g.durum} />
              {g.ilerleme && (
                <Typography variant="body2" color="text.secondary">
                  {g.ilerleme.tamamlanan}/{g.ilerleme.toplam}
                </Typography>
              )}
            </Stack>
          </AccordionSummary>
          <AccordionDetails>
            {g.ilerleme && (
              <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
                <LinearProgress
                  variant="determinate"
                  value={g.ilerleme.yuzde}
                  sx={{ flexGrow: 1, height: 8, borderRadius: 4 }}
                />
                <Typography variant="body2">%{g.ilerleme.yuzde}</Typography>
              </Stack>
            )}
            {g.aciklama && (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {g.aciklama}
              </Typography>
            )}

            <Stack spacing={1}>
              {g.maddeler.map((m) => {
                // Onaylanan madde kilitlenir; reddedilen yeniden isaretlenebilir.
                const kilitli = saltOkunur || m.durum === "ONAYLANDI";
                const isaretli = m.durum !== "BEKLIYOR" && m.durum !== "REDDEDILDI";
                return (
                  <Paper key={m.hashId} variant="outlined" sx={{ p: 1.5 }}>
                    <Stack direction="row" spacing={1} alignItems="flex-start">
                      <Checkbox
                        checked={isaretli}
                        disabled={kilitli || guncelle.isPending}
                        onChange={(e) =>
                          guncelle.mutate({ maddeHashId: m.hashId, tamamlandi: e.target.checked })
                        }
                        sx={{ mt: -1 }}
                      />
                      <Box sx={{ flexGrow: 1 }}>
                        <Typography variant="body2">{m.maddeMetni}</Typography>
                        {m.stajyerNotu && (
                          <Typography variant="caption" color="text.secondary" display="block">
                            Notum: {m.stajyerNotu}
                          </Typography>
                        )}
                        {/* Reddedilen maddede sorumlunun geri bildirimi asil bilgi — one cikar. */}
                        {m.onayNotu && (
                          <Alert
                            severity={m.durum === "REDDEDILDI" ? "warning" : "success"}
                            sx={{ mt: 1, py: 0 }}
                          >
                            {m.onayNotu}
                          </Alert>
                        )}
                      </Box>
                      <Stack direction="row" spacing={1} alignItems="center" sx={{ flexShrink: 0 }}>
                        <GorevDurumEtiketi durum={m.durum} />
                        <Button
                          size="small"
                          variant="outlined"
                          color="inherit"
                          startIcon={<EditNoteIcon />}
                          disabled={kilitli}
                          onClick={() => setNotDialog({ madde: m, not: m.stajyerNotu ?? "" })}
                        >
                          Not
                        </Button>
                      </Stack>
                    </Stack>
                  </Paper>
                );
              })}
            </Stack>
          </AccordionDetails>
        </Accordion>
      ))}

      <Dialog open={notDialog !== null} onClose={() => setNotDialog(null)} maxWidth="sm" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (notDialog) {
              // tamamlandi gondermiyoruz: not yazmak maddeyi tamamlamis saymamali,
              // isaretleme ayri bir eylem (checkbox).
              guncelle.mutate({ maddeHashId: notDialog.madde.hashId, not: notDialog.not });
            }
          }}
        >
          <DialogTitle>Madde Notu</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {notDialog?.madde.maddeMetni}
            </Typography>
            <TextField
              label="Notunuz"
              value={notDialog?.not ?? ""}
              onChange={(e) => setNotDialog((n) => (n ? { ...n, not: e.target.value } : n))}
              multiline
              minRows={3}
              fullWidth
              autoFocus
              inputProps={{ maxLength: 4000 }}
              helperText="Boş bırakıp kaydederseniz mevcut not silinir."
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setNotDialog(null)}>Vazgeç</Button>
            <IslemButonu type="submit" variant="contained" calisiyor={guncelle.isPending}>
              Kaydet
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>
    </>
  );
}
