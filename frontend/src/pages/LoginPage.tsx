import { Alert, Box, Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { login } from "../api/auth";
import { rolAnaSayfasi, useAuth } from "../auth/AuthContext";
import { useToast } from "../ui/ToastProvider";

/**
 * Giris ekrani. Iki cikis yolu var:
 *   - sifreDegistirmeli=true  -> token GELMEZ, /sifre-degistir'e gonderilir (ilk giris /
 *     sifirlanmis sifre). Oturum acilmaz, cunku elde token yok.
 *   - normal                  -> token saklanir, role gore panele gidilir.
 */
export function LoginPage() {
  const { oturumAc } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const konum = useLocation();
  const [arama] = useSearchParams();

  const [email, setEmail] = useState("");
  const [sifre, setSifre] = useState("");
  const [yukleniyor, setYukleniyor] = useState(false);

  // Interceptor 401'de buraya ?sebep=oturum ile atiyor.
  const oturumDustu = arama.get("sebep") === "oturum";
  // RequireRole nereden geldigimizi tasiyor; giristen sonra oraya donelim.
  const nereden = (konum.state as { nereden?: string } | null)?.nereden;

  async function gonder(e: React.FormEvent) {
    e.preventDefault();
    setYukleniyor(true);
    try {
      const yanit = await login({ email, sifre });
      if (yanit.sifreDegistirmeli) {
        // Gecici sifreyi de tasiyoruz: backend sifre-degistir'de kimligi email+eskiSifre
        // ile dogruluyor (henuz token yok), ama kullaniciya az once yazdigi sifreyi
        // ikinci kez yazdirmanin anlami yok.
        navigate("/sifre-degistir", { replace: true, state: { email, sifre } });
        return;
      }
      oturumAc(yanit, email);
      navigate(nereden ?? rolAnaSayfasi(yanit.rol), { replace: true });
    } catch (err) {
      toast.hata(err, "Giriş yapılamadı");
    } finally {
      setYukleniyor(false);
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", display: "grid", placeItems: "center", p: 2 }}>
      <Card sx={{ width: "100%", maxWidth: 400 }}>
        <CardContent>
          <Typography variant="h5" component="h1" gutterBottom>
            Staj Envanter
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Kurum e-postanız ve şifrenizle giriş yapın.
          </Typography>

          {oturumDustu && (
            <Alert severity="info" sx={{ mb: 2 }}>
              Oturumunuz sona erdi, tekrar giriş yapın.
            </Alert>
          )}

          <form onSubmit={gonder}>
            <Stack spacing={2}>
              <TextField
                label="E-posta"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="username"
                required
                autoFocus
                fullWidth
              />
              <TextField
                label="Şifre"
                type="password"
                value={sifre}
                onChange={(e) => setSifre(e.target.value)}
                autoComplete="current-password"
                required
                fullWidth
              />
              <Button type="submit" variant="contained" disabled={yukleniyor} fullWidth>
                {yukleniyor ? "Giriş yapılıyor..." : "Giriş Yap"}
              </Button>
            </Stack>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
