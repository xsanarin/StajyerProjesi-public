import { Skeleton, TableCell, TableRow } from "@mui/material";
import { useEffect, useState } from "react";

// Backend localde 20-50 ms'de donuyor; iskeleti hemen basarsak yanip sonuyor.
// Bu surenin altinda yanit gelirse hic gorunmesin.
const GECIKME_MS = 250;

/** true'ya ancak `ms` kadar bekledikten sonra doner; `aktif` once kapanirsa hic donmez. */
export function useGecikmeliGoster(aktif: boolean, ms = GECIKME_MS): boolean {
  const [goster, setGoster] = useState(false);

  useEffect(() => {
    if (!aktif) {
      setGoster(false);
      return;
    }
    const zamanlayici = window.setTimeout(() => setGoster(true), ms);
    return () => window.clearTimeout(zamanlayici);
  }, [aktif, ms]);

  return goster;
}

/**
 * Devredilmis (bayat) veri gosterilirken tabloyu soluklastirir.
 *
 * placeholderData kullanan listelerde anahtar degistigi an yeni istek daha
 * donmemistir ve ekranda ONCEKI anahtarin verisi durur: arama kutusu "ahmet"
 * yazarken satirlar hala "murat"in sonucu olabilir. Lokalde 50 ms surdugu icin
 * fark edilmez, uzak sunucuda saniyelere cikar ve kullanici yanlis sonucu
 * okumus olur. Soluk tablo "bu henuz kesin degil" sinyali veriyor; gercek yanit
 * gelince netlesir. Kaynak: useQuery(...).isPlaceholderData.
 */
export function bayatVeriSx(bayat: boolean) {
  return { opacity: bayat ? 0.5 : 1, transition: "opacity 150ms" };
}

/**
 * Tablo govdesinde "Yukleniyor..." yerine gecen gri satirlar. Tablo yuksekligi
 * bastan dolar, veri gelince satirlar zıplamaz.
 */
export function TabloIskeleti({
  sutun,
  satir = 5,
}: {
  sutun: number;
  satir?: number;
}) {
  return (
    <>
      {Array.from({ length: satir }, (_, s) => (
        <TableRow key={s}>
          {Array.from({ length: sutun }, (_, h) => (
            <TableCell key={h}>
              <Skeleton variant="text" />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  );
}
