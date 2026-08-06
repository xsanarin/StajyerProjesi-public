import {
  Alert,
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
import { useMemo, useState } from "react";
import { btTaleplerim } from "../../api/sorumlu";
import { AramaKutusu, aramaEslesiyor } from "../../ui/AramaKutusu";
import { TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";

/**
 * Sorumlunun BT talep ekrani — SALT OKUNUR.
 *
 * Talep olusturma/duzenleme admin yetkisinde, o yuzden burada hicbir yazma butonu
 * yok. Backend tarafinda da /api/sorumlu/talepler yalnizca GET; kapatilmis bir buton
 * degil, hic olmayan bir uc.
 *
 * Gorulen liste sorumlunun DEPARTMANINA aittir, kendisine degil: ayni departmandaki
 * butun sorumlular ayni talepleri gorur.
 */
export function BtTalepleriPage() {
  const liste = useQuery({ queryKey: ["sorumlu", "bt-talepler"], queryFn: btTaleplerim });
  const [arama, setArama] = useState("");

  const gorunenler = useMemo(
    () =>
      (liste.data ?? []).filter((t) =>
        aramaEslesiyor([t.baslik, t.aciklama ?? "", t.stajyerAdSoyad ?? ""], arama),
      ),
    [liste.data, arama],
  );
  const iskeletGoster = useGecikmeliGoster(liste.isPending);

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          BT Talepleri
        </Typography>
        <AramaKutusu deger={arama} onDegis={setArama} placeholder="Talep ara..." />
      </Stack>

      {/* Liste bos oldugunda "hata mi, gercekten bos mu" ayrimi kullaniciya birakilmasin. */}
      {!liste.isPending && !liste.isError && liste.data?.length === 0 && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Departmanınıza açılmış BT talebi yok.
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Başlık</TableCell>
              {/* Departman kolonu yok (hepsi kendi departmani) ama stajyer var:
                  talep departman geneline mi yoksa belirli bir kisiye mi acildi,
                  sorumlunun ilk bakista gormesi gereken sey bu. */}
              <TableCell>Stajyer</TableCell>
              <TableCell>Açıklama</TableCell>
              <TableCell width={150}>Oluşturulma</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={4} />}
            {liste.isError && (
              <TableRow>
                <TableCell colSpan={4}>Liste alınamadı.</TableCell>
              </TableRow>
            )}
            {gorunenler.map((t) => (
              <TableRow key={t.hashId} hover>
                <TableCell>{t.baslik}</TableCell>
                <TableCell>{t.stajyerAdSoyad ?? "-"}</TableCell>
                <TableCell sx={{ whiteSpace: "pre-wrap" }}>{t.aciklama ?? "-"}</TableCell>
                <TableCell>{t.olusturmaTarihi}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}
