import { Alert, Box, Divider, Paper, Stack, TextField, Typography } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { degerlendirmem, puanla } from "../../api/sorumlu";
import type { StajResponse } from "../../api/types";
import { IslemButonu } from "../../ui/IslemButonu";
import { useToast } from "../../ui/ToastProvider";

/**
 * Puanlama. Backend kurallari (PuanlamaService):
 *   - yalnizca ASIL sorumlu puanlayabilir,
 *   - puan 0-100,
 *   - puanlanan staj TAMAMLANDI'ya gecer ve kilitlenir; duzeltme admin isi.
 * Bu yuzden ekran "tek seferlik" gibi davranir; hata gelirse toast'ta backend mesaji cikar.
 */
export function PuanSekmesi({ staj }: { staj: StajResponse }) {
  const toast = useToast();
  const qc = useQueryClient();

  const [puan, setPuan] = useState("");
  const [yorum, setYorum] = useState("");

  // Verilmis puani gorebilmek icin: puanladiktan sonra staj kilitlenir ve sorumlu
  // kendi verdigi puani baska hicbir yerde goremez.
  const mevcut = useQuery({
    queryKey: ["sorumlu", "degerlendirme", staj.hashId],
    queryFn: () => degerlendirmem(staj.hashId),
  });

  const kaydet = useMutation({
    mutationFn: () => puanla(staj.hashId, { puan: Number(puan), yorum: yorum || null }),
    onSuccess: (d) => {
      qc.invalidateQueries({ queryKey: ["sorumlu", "stajlar"] });
      qc.invalidateQueries({ queryKey: ["sorumlu", "degerlendirme", staj.hashId] });
      toast.basari(`Puan kaydedildi: ${d.puan}`);
      setPuan("");
      setYorum("");
    },
    onError: (err) => toast.hata(err, "Puan kaydedilemedi"),
  });

  const sayi = Number(puan);
  const puanGecersiz = puan !== "" && (Number.isNaN(sayi) || sayi < 0 || sayi > 100);
  const kilitli = staj.durum !== "DEVAM_EDIYOR";

  return (
    <Paper sx={{ p: 3, maxWidth: 520 }}>
      <Typography variant="h6" gutterBottom>
        Stajı Puanla
      </Typography>

      {mevcut.data && mevcut.data.puan != null && (
        <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
          <Typography variant="subtitle2" gutterBottom>
            Verilmiş puan
          </Typography>
          <Typography variant="h4">{mevcut.data.puan}</Typography>
          {mevcut.data.yorum && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {mevcut.data.yorum}
            </Typography>
          )}
          {/* Gerekce doluysa puani admin degistirmis demektir; sorumlu bunu bilmeli. */}
          {mevcut.data.duzeltmeGerekcesi && (
            <>
              <Divider sx={{ my: 1.5 }} />
              <Typography variant="caption" color="text.secondary">
                Admin düzeltmesi: {mevcut.data.duzeltmeGerekcesi}
              </Typography>
            </>
          )}
        </Paper>
      )}

      {kilitli && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Bu staj {staj.durum} durumunda. Puanlanmış bir staj yeniden puanlanamaz; düzeltme
          gerekiyorsa admin kilidi açmalı.
        </Alert>
      )}

      <Box
        component="form"
        onSubmit={(e) => {
          e.preventDefault();
          kaydet.mutate();
        }}
      >
        <Stack spacing={2}>
          <TextField
            label="Puan (0-100)"
            type="number"
            value={puan}
            onChange={(e) => setPuan(e.target.value)}
            inputProps={{ min: 0, max: 100, step: "0.5" }}
            error={puanGecersiz}
            helperText={puanGecersiz ? "Puan 0 ile 100 arasında olmalı." : " "}
            required
            fullWidth
          />
          <TextField
            label="Yorum"
            value={yorum}
            onChange={(e) => setYorum(e.target.value)}
            multiline
            minRows={3}
            fullWidth
          />
          <IslemButonu
            type="submit"
            variant="contained"
            calisiyor={kaydet.isPending}
            disabled={puan === "" || puanGecersiz}
          >
            Puanla ve Stajı Kapat
          </IslemButonu>
        </Stack>
      </Box>
    </Paper>
  );
}