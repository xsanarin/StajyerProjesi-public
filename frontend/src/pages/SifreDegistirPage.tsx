import { Alert, Box, Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { sifreDegistir } from "../api/auth";
import { rolAnaSayfasi, useAuth } from "../auth/AuthContext";
import { useToast } from "../ui/ToastProvider";

/**
 * Ilk giris / sifirlanmis sifre ekrani. Buraya login'den `state.email` + `state.sifre`
 * ile gelinir; dogrudan URL yazilarak gelinirse ikisi de bos olur ve kullanici elle girer.
 *
 * Gecici sifre router state'inde, yani history.state'te tasinir: localStorage'a
 * yazilmaz ama sayfa yenilense de o gecmis kaydinda kalir. Zaten bu ekranda
 * degistirilip gecersiz kilinacagi icin kabul edilebilir bir odunc.
 *
 * Basarili degisiklik token DOLU bir LoginResponse dondurur — ayrica login
 * yaptirmaya gerek yok, oturumu burada aciyoruz.
 */
export function SifreDegistirPage() {
  const { oturumAc } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const konum = useLocation();

  const gelenState = konum.state as { email?: string; sifre?: string } | null;
  const gelenEmail = gelenState?.email ?? "";
  const gelenSifre = gelenState?.sifre ?? "";
  const [email, setEmail] = useState(gelenEmail);
  const [eskiSifre, setEskiSifre] = useState(gelenSifre);
  // Login'den geldiysek gecici sifre elimizde: alani hic gostermiyoruz. Dogrudan URL
  // yazilarak gelinirse state bos olur ve alan geri gelir.
  const geciciSifreBiliniyor = gelenSifre !== "";
  const [yeniSifre, setYeniSifre] = useState("");
  const [yeniSifreTekrar, setYeniSifreTekrar] = useState("");
  const [yukleniyor, setYukleniyor] = useState(false);

  const tekrarHatali = yeniSifreTekrar.length > 0 && yeniSifre !== yeniSifreTekrar;
  const kisaSifre = yeniSifre.length > 0 && yeniSifre.length < 6;

  async function gonder(e: React.FormEvent) {
    e.preventDefault();
    if (tekrarHatali || kisaSifre) {
      return;
    }
    setYukleniyor(true);
    try {
      const yanit = await sifreDegistir({ email, eskiSifre, yeniSifre });
      if (!yanit.token) {
        // Beklenmiyor ama olursa kullaniciyi bos ekranda birakmayalim.
        toast.bilgi("Şifreniz değişti, tekrar giriş yapın.");
        navigate("/login", { replace: true });
        return;
      }
      oturumAc(yanit, email);
      toast.basari("Şifreniz güncellendi.");
      navigate(rolAnaSayfasi(yanit.rol), { replace: true });
    } catch (err) {
      toast.hata(err, "Şifre değiştirilemedi");
    } finally {
      setYukleniyor(false);
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", display: "grid", placeItems: "center", p: 2 }}>
      <Card sx={{ width: "100%", maxWidth: 400 }}>
        <CardContent>
          <Typography variant="h5" component="h1" gutterBottom>
            Şifre belirleyin
          </Typography>
          <Alert severity="info" sx={{ mb: 2 }}>
            Devam etmeden önce geçici şifrenizi değiştirmeniz gerekiyor.
          </Alert>

          <form onSubmit={gonder}>
            <Stack spacing={2}>
              <TextField
                label="E-posta"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="username"
                required
                fullWidth
              />
              {!geciciSifreBiliniyor && (
                <TextField
                  label="Geçici şifre"
                  type="password"
                  value={eskiSifre}
                  onChange={(e) => setEskiSifre(e.target.value)}
                  autoComplete="current-password"
                  required
                  autoFocus={gelenEmail !== ""}
                  fullWidth
                />
              )}
              <TextField
                label="Yeni şifre"
                type="password"
                value={yeniSifre}
                onChange={(e) => setYeniSifre(e.target.value)}
                autoComplete="new-password"
                required
                autoFocus={geciciSifreBiliniyor}
                error={kisaSifre}
                helperText={kisaSifre ? "En az 6 karakter olmalı" : " "}
                fullWidth
              />
              <TextField
                label="Yeni şifre (tekrar)"
                type="password"
                value={yeniSifreTekrar}
                onChange={(e) => setYeniSifreTekrar(e.target.value)}
                autoComplete="new-password"
                required
                error={tekrarHatali}
                helperText={tekrarHatali ? "Şifreler eşleşmiyor" : " "}
                fullWidth
              />
              <Button
                type="submit"
                variant="contained"
                disabled={yukleniyor || tekrarHatali || kisaSifre}
                fullWidth
              >
                {yukleniyor ? "Kaydediliyor..." : "Şifreyi Değiştir"}
              </Button>
            </Stack>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}