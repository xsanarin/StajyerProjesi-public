import {
  Alert,
  Button,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { benimStajlarim, yaklasanBitisler } from "../../api/sorumlu";
import { TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";

/**
 * Sorumlunun acilis ekrani: kendi stajyerleri + yaklasan bitis uyarilari.
 * Iki liste de kimligi token'dan alan uclardan gelir; baskasinin stajyeri gorunmez.
 */
export function SupervisorHomePage() {
  const stajlar = useQuery({ queryKey: ["sorumlu", "stajlar"], queryFn: benimStajlarim });
  const bitisler = useQuery({
    queryKey: ["sorumlu", "yaklasan-bitisler"],
    queryFn: () => yaklasanBitisler(5),
  });
  const iskeletGoster = useGecikmeliGoster(stajlar.isPending);

  return (
    <>
      <Typography variant="h5" component="h1" gutterBottom>
        Stajyerlerim
      </Typography>

      {bitisler.data && bitisler.data.length > 0 && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          <Stack spacing={0.5}>
            {bitisler.data.map((b) => (
              <span key={b.stajHashId}>
                <strong>{b.stajyerAdSoyad}</strong> — stajın bitmesine {b.gunKalan} gün
                ({b.stajBitisTarihi}). Puanlamayı unutmayın.
              </span>
            ))}
          </Stack>
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Stajyer</TableCell>
              <TableCell>Dönem</TableCell>
              <TableCell>Departman</TableCell>
              {/* Kendi adimizi hangi sutunda gordugumuz asil mi yedek mi oldugumuzu soyler;
                  staj yaniti sorumlu hashId'lerini isimle birlikte verdigi halde bizim kendi
                  hashId'miz istemcide yok, o yuzden "rolum" diye tek sutun uretemiyoruz. */}
              <TableCell>Asıl</TableCell>
              <TableCell>Yedek</TableCell>
              <TableCell>Seviye</TableCell>
              <TableCell>Tarihler</TableCell>
              <TableCell>Durum</TableCell>
              <TableCell align="right" width={100} />
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={9} />}
            {stajlar.isError && (
              <TableRow>
                <TableCell colSpan={9}>Liste alınamadı.</TableCell>
              </TableRow>
            )}
            {stajlar.data?.length === 0 && (
              <TableRow>
                <TableCell colSpan={9}>Size atanmış stajyer yok.</TableCell>
              </TableRow>
            )}
            {stajlar.data?.map((s) => (
              <TableRow key={s.hashId} hover>
                <TableCell>{s.stajyerAdSoyad}</TableCell>
                <TableCell>{s.donemAd}</TableCell>
                <TableCell>{s.departmanAd}</TableCell>
                <TableCell>{s.asilSorumluAdSoyad ?? "-"}</TableCell>
                <TableCell>{s.yedekSorumluAdSoyad ?? "-"}</TableCell>
                <TableCell>{s.seviye ?? "-"}</TableCell>
                <TableCell sx={{ whiteSpace: "nowrap" }}>
                  {s.stajBaslangicTarihi ?? "?"} - {s.stajBitisTarihi ?? "?"}
                </TableCell>
                <TableCell>
                  <Chip
                    size="small"
                    label={s.durum}
                    color={s.durum === "DEVAM_EDIYOR" ? "primary" : "default"}
                  />
                </TableCell>
                <TableCell align="right">
                  <Button size="small" component={Link} to={`/supervisor/interns/${s.hashId}`}>
                    Aç
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}
