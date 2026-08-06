import { TablePagination } from "@mui/material";

/** Varsayilan 25; backend boyutu 100'de kirptigi icin ustu teklif edilmiyor. */
export const SAYFA_BOYUTLARI = [10, 25, 50, 100];

/**
 * "1-1 / 1" gibi yazimlar gereksiz karmasik duruyordu: tek kayitta araliğın iki
 * ucu ayni sayi oluyor ve okuyan once "neden iki kere 1?" diye takiliyor.
 *
 *   0 kayit      -> "Kayit yok"      (eskiden "0-0 / 0")
 *   1 kayit      -> "1 kayit"        (eskiden "1-1 / 1")
 *   tek satirlik -> "41 / 41"        (son sayfada tek kayit kaldiginda)
 *   digerleri    -> "1-10 / 45"      (standart aralik gosterimi)
 */
function araligiYaz(baslangic: number, bitis: number, toplam: number): string {
  if (toplam === 0) {
    return "Kayıt yok";
  }
  if (toplam === 1) {
    return "1 kayıt";
  }
  if (baslangic === bitis) {
    return `${baslangic} / ${toplam}`;
  }
  return `${baslangic}-${bitis} / ${toplam}`;
}

/**
 * Sunucu tarafli listelerin ortak sayfa cubugu.
 *
 * `toplamKayit` sunucudan gelir — elimizdeki satir sayisi degil, filtreye uyan
 * TUM kayitlarin sayisidir; sayfa sayisini MUI bundan hesaplar.
 */
export function SayfaCubugu({
  toplamKayit,
  sayfa,
  boyut,
  onSayfaDegis,
  onBoyutDegis,
}: {
  toplamKayit: number;
  sayfa: number;
  boyut: number;
  onSayfaDegis: (yeni: number) => void;
  onBoyutDegis: (yeni: number) => void;
}) {
  return (
    <TablePagination
      component="div"
      count={toplamKayit}
      page={sayfa}
      rowsPerPage={boyut}
      rowsPerPageOptions={SAYFA_BOYUTLARI}
      onPageChange={(_, yeniSayfa) => onSayfaDegis(yeniSayfa)}
      onRowsPerPageChange={(e) => onBoyutDegis(Number(e.target.value))}
      // "Sayfa basina" tek basina yarim cumle kaliyordu; neyin sayisi oldugu yazili.
      labelRowsPerPage="Sayfa başına satır:"
      labelDisplayedRows={({ from, to, count }) => araligiYaz(from, to, count)}
      getItemAriaLabel={(tur) => (tur === "previous" ? "Önceki sayfa" : "Sonraki sayfa")}
      sx={{
        // Etiket ve sayac tablo verisiyle yarismasin diye sonuk; ama okunur boyutta.
        "& .MuiTablePagination-selectLabel, & .MuiTablePagination-displayedRows": {
          color: "text.secondary",
          fontSize: 13,
        },
        // Secili boyut bir DEGER, etiket degil — ana metin renginde ve biraz kalin.
        // Ayrica sayi ile acilir ok bitisikti, okun soluna nefes payi veriliyor.
        "& .MuiTablePagination-select": {
          color: "text.primary",
          fontWeight: 500,
          paddingRight: "34px",
        },
        "& .MuiTablePagination-selectIcon": { right: 6 },
        // Oklar arka planla neredeyse ayni tondaydi: tiklanabilir mi, pasif mi
        // ayirt edilemiyordu. Aktif ok ana metin tonuna yakin, pasif olan acikca
        // soluk, uzerine gelince zemin beliriyor.
        "& .MuiTablePagination-actions": {
          marginLeft: 2,
          "& .MuiIconButton-root": {
            color: "text.secondary",
            transition: "background-color 150ms, color 150ms",
            "&:hover": { backgroundColor: "action.hover", color: "text.primary" },
            "&.Mui-disabled": { color: "action.disabled" },
          },
        },
      }}
    />
  );
}
