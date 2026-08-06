import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import type { ProvisionResponse } from "../api/types";
import { useToast } from "./ToastProvider";

/**
 * Gecici sifre yanittan SADECE BIR KEZ gelir, hicbir ucta tekrar okunamaz.
 * Bu yuzden ayri bir dialogda gosterip kullanicinin kapatmasini bekliyoruz —
 * toast olsaydi 4 saniyede kayip giderdi. Sorumlu ve stajyer olusturma
 * sayfalarinin ikisi de ayni yanit tipini aldigi icin ortak bilesen.
 */
export function GeciciSifreDialog({
  provizyon,
  onKapat,
}: {
  provizyon: ProvisionResponse | null;
  onKapat: () => void;
}) {
  const toast = useToast();

  return (
    <Dialog open={provizyon !== null} onClose={onKapat} maxWidth="xs" fullWidth>
      <DialogTitle>Hesap Oluşturuldu</DialogTitle>
      <DialogContent>
        <Alert severity="warning" sx={{ mb: 2 }}>
          Geçici şifre yalnızca bir kez gösterilir. Kullanıcıya iletin; ilk girişinde
          değiştirmesi istenecek.
        </Alert>
        <Stack spacing={1}>
          <Typography variant="body2">
            <strong>E-posta:</strong> {provizyon?.email}
          </Typography>
          <Typography variant="body2">
            <strong>Rol:</strong> {provizyon?.rol}
          </Typography>
          <TextField
            label="Geçici şifre"
            value={provizyon?.geciciSifre ?? ""}
            InputProps={{ readOnly: true }}
            onFocus={(e) => e.target.select()}
            fullWidth
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={() => {
            if (provizyon) {
              // navigator.clipboard yalnizca guvenli baglamda (https / localhost) var;
              // yoksa kullanici alandan elle kopyalar, o yuzden sessizce gecistiriyoruz.
              navigator.clipboard?.writeText(provizyon.geciciSifre).then(
                () => toast.basari("Şifre panoya kopyalandı."),
                () => toast.bilgi("Kopyalanamadı, şifreyi elle seçip kopyalayın."),
              );
            }
          }}
        >
          Kopyala
        </Button>
        <Button variant="contained" onClick={onKapat}>
          Kapat
        </Button>
      </DialogActions>
    </Dialog>
  );
}
