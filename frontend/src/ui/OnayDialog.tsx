import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from "@mui/material";
import { IslemButonu } from "./IslemButonu";

/**
 * Geri alinamayan islemler icin ortak onay kutusu (silme, sorumlu ayirma).
 * window.confirm yerine: metni bicimlendirebilelim ve islem surerken butonu kilitleyebilelim.
 */
export function OnayDialog({
  acik,
  baslik,
  metin,
  onayMetni = "Evet",
  calisiyor = false,
  onOnay,
  onKapat,
}: {
  acik: boolean;
  baslik: string;
  metin: string;
  onayMetni?: string;
  calisiyor?: boolean;
  onOnay: () => void;
  onKapat: () => void;
}) {
  return (
    <Dialog open={acik} onClose={calisiyor ? undefined : onKapat} maxWidth="xs" fullWidth>
      <DialogTitle>{baslik}</DialogTitle>
      <DialogContent>
        <Typography variant="body2">{metin}</Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onKapat} disabled={calisiyor}>
          Vazgeç
        </Button>
        <IslemButonu onClick={onOnay} color="error" variant="contained" calisiyor={calisiyor}>
          {onayMetni}
        </IslemButonu>
      </DialogActions>
    </Dialog>
  );
}