import {
  Box,
  FormControlLabel,
  Paper,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
  useTheme,
} from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { departmanIstatistigi } from "../../api/admin";

/**
 * Her departman ayri renk (kullanici karari). Ilk sekiz slot dataviz
 * kilavuzunun dogrulanmis kategorik paleti; dokuzdan itibaren ayni sekiz hue'nun
 * ikinci turu — acik temada koyulastirilmis, koyu temada aciklastirilmis
 * (koyu zeminde koyulastirmak kontrasti 3:1'in altina dusuruyordu).
 *
 * DIKKAT: sekizden fazla hue hicbir sekilde renk korlugu testlerini gecemez;
 * dogrulayici bu 16'lik seti FAIL veriyor ve bu kacinilmaz. Bu yuzden kimlik
 * ASLA renge birakilmiyor: yanindaki listede her departmanin adi ve sayisi
 * yaziyor, dilimle satir karsilikli vurgulaniyor.
 */
const SERI_RENKLERI = {
  light: [
    "#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#008300", "#4a3aa7", "#e34948",
    "#18467c", "#883c1e", "#106647", "#895d00", "#87475f", "#004c00", "#2b2261", "#842a2a",
  ],
  dark: [
    "#3987e5", "#d95926", "#199e70", "#c98500", "#d55181", "#008300", "#9085e9", "#e66767",
    "#84b5ef", "#e79878", "#70c3a6", "#deb361", "#e593b1", "#61b261", "#bab3f1", "#f0a1a1",
  ],
};

// Halka geometrisi. Dilimler arasindaki 2px'lik bosluk yuzey renginde —
// dilimleri ayiran sey cerceve degil, bosluk.
const MERKEZ = 100;
const YARICAP = 74;
const KALINLIK = 30;
const CEVRE = 2 * Math.PI * YARICAP;
const BOSLUK = 2;

type Dilim = { ad: string; sayi: number; renk: string };

export function IstatistiklerPage() {
  const theme = useTheme();
  const koyu = theme.palette.mode === "dark";
  const [sadeceAktif, setSadeceAktif] = useState(true);
  const [vurgulanan, setVurgulanan] = useState<number | null>(null);

  /**
   * Sayim SUNUCUDA yapiliyor. Eskiden tum staj listesi cekilip departman basina
   * benzersiz stajyer Set ile burada hesaplaniyordu; birkac bin stajda bu hem
   * gereksiz veri hem de bosuna is. Uc zaten cok-a-bir azaltilmis satir donuyor
   * (departman sayisi kadar), sirali olarak.
   */
  const istatistik = useQuery({
    queryKey: ["staj-istatistik", sadeceAktif],
    queryFn: () => departmanIstatistigi(sadeceAktif),
  });

  const { dilimler, toplam } = useMemo(() => {
    // Renk departmanin kimligi: siraya gore sabit atanir, dongusel tekrar
    // ancak 16 departmani astiktan sonra baslar.
    const renkler = koyu ? SERI_RENKLERI.dark : SERI_RENKLERI.light;
    const sonuc: Dilim[] = (istatistik.data ?? []).map((d, i) => ({
      ad: d.departmanAd,
      sayi: d.stajyerSayisi,
      renk: renkler[i % renkler.length],
    }));

    return { dilimler: sonuc, toplam: sonuc.reduce((t, d) => t + d.sayi, 0) };
  }, [istatistik.data, koyu]);

  // Halkanin ortasi hem baslik rakami hem de ipucu. Varsayilan olarak TOPLAM
  // gosteriliyor: ilk siradaki departmanlar esit oldugunda merkezde tek bir ad
  // yazmak olmayan bir kazanan ima ederdi.
  const odak = vurgulanan !== null ? dilimler[vurgulanan] : null;

  /**
   * "En cok nerede" sorusunun duz cevabi. Esitlik gercek bir durum (test
   * verisinde ilk alti departman 2'ser stajyer), o yuzden tek ad uydurmak
   * yerine esitligi soyluyoruz.
   */
  const lider = useMemo(() => {
    if (dilimler.length === 0) {
      return null;
    }
    const enYuksek = dilimler[0].sayi;
    const esitler = dilimler.filter((d) => d.sayi === enYuksek);
    return { sayi: enYuksek, adlar: esitler.map((d) => d.ad) };
  }, [dilimler]);

  if (istatistik.isPending) {
    return <Typography>Yükleniyor...</Typography>;
  }

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          Stajyer Dağılımı
        </Typography>
        <FormControlLabel
          control={
            <Switch
              size="small"
              checked={sadeceAktif}
              onChange={(e) => setSadeceAktif(e.target.checked)}
            />
          }
          label="Sadece devam edenler"
        />
      </Stack>

      {toplam === 0 ? (
        <Paper sx={{ p: 3 }}>
          <Typography color="text.secondary">
            {sadeceAktif
              ? "Devam eden staj yok. Geçmişi görmek için üstteki anahtarı kapatın."
              : "Hiç staj kaydı yok."}
          </Typography>
        </Paper>
      ) : (
        <Paper sx={{ p: 3 }}>
          {lider && (
            <Typography sx={{ mb: 3 }}>
              {lider.adlar.length === 1 ? (
                <>
                  En çok stajyer <strong>{lider.adlar[0]}</strong> departmanında —{" "}
                  <strong>{lider.sayi}</strong> stajyer.
                </>
              ) : (
                <>
                  İlk sırada <strong>{lider.adlar.length} departman eşit</strong>, her birinde{" "}
                  <strong>{lider.sayi}</strong> stajyer: {lider.adlar.join(", ")}.
                </>
              )}
            </Typography>
          )}
          <Stack
            direction={{ xs: "column", md: "row" }}
            spacing={4}
            alignItems={{ md: "center" }}
          >
            <Box sx={{ width: 260, flexShrink: 0, mx: "auto" }}>
              <Box component="svg" viewBox="0 0 200 200" sx={{ width: "100%", display: "block" }}>
                {/* -90 derece: ilk dilim saat 12'den baslasin. */}
                <g transform={`rotate(-90 ${MERKEZ} ${MERKEZ})`}>
                  {dilimler.map((d, i) => {
                    const onceki = dilimler
                      .slice(0, i)
                      .reduce((t, x) => t + x.sayi, 0);
                    const uzunluk = (d.sayi / toplam) * CEVRE;
                    // Tek dilim varsa bosluk birakmak halkayi kirar.
                    const gorunen =
                      dilimler.length === 1 ? uzunluk : Math.max(uzunluk - BOSLUK, 1);
                    return (
                      <circle
                        key={d.ad}
                        cx={MERKEZ}
                        cy={MERKEZ}
                        r={YARICAP}
                        fill="none"
                        stroke={d.renk}
                        strokeWidth={vurgulanan === i ? KALINLIK + 6 : KALINLIK}
                        strokeDasharray={`${gorunen} ${CEVRE - gorunen}`}
                        strokeDashoffset={-(onceki / toplam) * CEVRE}
                        onMouseEnter={() => setVurgulanan(i)}
                        onMouseLeave={() => setVurgulanan(null)}
                        style={{ transition: "stroke-width 120ms", cursor: "default" }}
                      />
                    );
                  })}
                </g>
                {/* Merkezdeki rakam sayfanin tek "hero" degeri. */}
                <text
                  x={MERKEZ}
                  y={MERKEZ - 4}
                  textAnchor="middle"
                  fill={theme.palette.text.primary}
                  style={{ font: "600 34px system-ui, -apple-system, Segoe UI, sans-serif" }}
                >
                  {odak ? odak.sayi : toplam}
                </text>
                <text
                  x={MERKEZ}
                  y={MERKEZ + 18}
                  textAnchor="middle"
                  fill={theme.palette.text.secondary}
                  style={{ font: "400 11px system-ui, -apple-system, Segoe UI, sans-serif" }}
                >
                  {odak ? "stajyer" : "toplam stajyer"}
                </text>
              </Box>
              <Typography
                variant="body2"
                color="text.secondary"
                align="center"
                sx={{ mt: 1, minHeight: 20 }}
                noWrap
              >
                {odak?.ad ?? ""}
              </Typography>
            </Box>

            {/* Liste hem lejant hem tablo gorunumu: kimlik yalnizca renge
                birakilmiyor ve acik temada dusuk kontrastli dilimler icin
                kilavuzun istedigi "gorunur etiket" karsilanmis oluyor. */}
            <Box sx={{ flexGrow: 1, minWidth: 0, width: "100%" }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Departman</TableCell>
                    <TableCell align="right">Stajyer</TableCell>
                    <TableCell align="right">Pay</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {dilimler.map((d, i) => (
                    <TableRow
                      key={d.ad}
                      hover
                      onMouseEnter={() => setVurgulanan(i)}
                      onMouseLeave={() => setVurgulanan(null)}
                    >
                      <TableCell>
                        <Stack direction="row" alignItems="center" spacing={1.5}>
                          <Box
                            sx={{
                              width: 12,
                              height: 12,
                              borderRadius: "50%",
                              bgcolor: d.renk,
                              flexShrink: 0,
                            }}
                          />
                          <Typography variant="body2" noWrap>
                            {d.ad}
                          </Typography>
                        </Stack>
                      </TableCell>
                      <TableCell align="right" sx={{ fontVariantNumeric: "tabular-nums" }}>
                        {d.sayi}
                      </TableCell>
                      <TableCell align="right" sx={{ fontVariantNumeric: "tabular-nums" }}>
                        %{Math.round((d.sayi / toplam) * 100)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Box>
          </Stack>
        </Paper>
      )}
    </>
  );
}
