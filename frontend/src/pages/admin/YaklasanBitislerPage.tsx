import {
  Alert,
  Chip,
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
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { adminYaklasanBitisler } from "../../api/admin";
import { AramaKutusu } from "../../ui/AramaKutusu";
import { bayatVeriSx, TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";
import { useGecikmeliDeger } from "../../ui/useGecikmeliDeger";

const GUN_SECENEKLERI = [3, 5, 7, 14, 30];

/**
 * Bitisi yaklasan stajlar. Ayni veriyi sorumlu da kendi ekraninda gorur
 * (/api/sorumlu/yaklasan-bitisler) ama orada yalnizca KENDI stajyerleri cikar;
 * buradaki admin ucu tum departmanlari kapsar.
 */
export function YaklasanBitislerPage() {
  const [gun, setGun] = useState(5);
  const [arama, setArama] = useState("");
  // Arama sunucuda: her tus vurusunda istek atmamak icin terim gecikmeli gidiyor.
  const gecikmeliArama = useGecikmeliDeger(arama);

  const liste = useQuery({
    queryKey: ["admin", "yaklasan-bitisler", gun, gecikmeliArama],
    queryFn: () => adminYaklasanBitisler(gun, gecikmeliArama),
    // Terim degisirken onceki sonuc ekranda kalsin; tablo bos/iskelet arasi zipzip etmesin.
    placeholderData: (onceki) => onceki,
  });
  const iskeletGoster = useGecikmeliGoster(liste.isPending);

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          Yaklaşan Bitişler
        </Typography>
        <AramaKutusu
          deger={arama}
          onDegis={setArama}
          placeholder="Stajyer, departman, sorumlu ara..."
        />
        <TextField
          select
          size="small"
          label="Kalan gün"
          value={gun}
          onChange={(e) => setGun(Number(e.target.value))}
          sx={{ width: 160 }}
        >
          {GUN_SECENEKLERI.map((g) => (
            <MenuItem key={g} value={g}>
              {g} gün içinde
            </MenuItem>
          ))}
        </TextField>
      </Stack>

      <Alert severity="info" sx={{ mb: 2 }}>
        Bitişine {gun} günden az kalan stajlar listelenir. Staj kapanmadan önce sorumlunun
        puanlaması gerekir; puanlanmayan staj DEVAM_EDIYOR durumunda kalır.
      </Alert>

      <TableContainer component={Paper} sx={bayatVeriSx(liste.isPlaceholderData)}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Stajyer</TableCell>
              <TableCell>Departman</TableCell>
              <TableCell>Bitiş tarihi</TableCell>
              <TableCell>Kalan</TableCell>
              <TableCell>Asıl sorumlu</TableCell>
              <TableCell>Yedek sorumlu</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={6} />}
            {liste.isError && (
              <TableRow>
                <TableCell colSpan={6}>Liste alınamadı.</TableCell>
              </TableRow>
            )}
            {liste.data?.map((b) => (
              <TableRow key={b.stajHashId} hover>
                <TableCell>{b.stajyerAdSoyad}</TableCell>
                <TableCell>{b.departmanAd}</TableCell>
                <TableCell>{b.stajBitisTarihi}</TableCell>
                <TableCell>
                  {/* Aciliyet renkle de anlatilsin; yalnizca sayiya bakmak gerekmesin. */}
                  <Chip
                    size="small"
                    label={b.gunKalan <= 0 ? "Bugün" : `${b.gunKalan} gün`}
                    color={b.gunKalan <= 1 ? "error" : b.gunKalan <= 3 ? "warning" : "default"}
                  />
                </TableCell>
                <TableCell>{b.asilSorumluAdSoyad ?? "-"}</TableCell>
                {/* Yedek yoksa asil sorumlu izinliyse puanlayacak kimse kalmaz — goze carpsin. */}
                <TableCell>
                  {b.yedekSorumluAdSoyad ?? (
                    <Typography variant="body2" color="warning.main">
                      yedek yok
                    </Typography>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}
