import ClearIcon from "@mui/icons-material/Clear";
import SearchIcon from "@mui/icons-material/Search";
import { IconButton, InputAdornment, TextField } from "@mui/material";

/**
 * Liste ekranlarinin ortak arama kutusu. Ayni blok once Stajlar ve
 * Departmanlar'a kopyalanmisti; her yeni listede tekrar yazmak yerine tek yerden.
 */
export function AramaKutusu({
  deger,
  onDegis,
  placeholder,
  genislik = 260,
}: {
  deger: string;
  onDegis: (yeni: string) => void;
  placeholder: string;
  genislik?: number;
}) {
  return (
    <TextField
      size="small"
      placeholder={placeholder}
      value={deger}
      onChange={(e) => onDegis(e.target.value)}
      sx={{ width: genislik }}
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon fontSize="small" />
          </InputAdornment>
        ),
        endAdornment: deger ? (
          <InputAdornment position="end">
            <IconButton size="small" aria-label="Aramayı temizle" onClick={() => onDegis("")}>
              <ClearIcon fontSize="small" />
            </IconButton>
          </InputAdornment>
        ) : null,
      }}
    />
  );
}

/**
 * Arama sorgusunu verilen alanlarla karsilastirir.
 *
 * Kucultmede locale SART: varsayilan toLowerCase "I" harfini "i" yapar, bu
 * yuzden "İstanbul" aramasi "istanbul" ile eslesmezdi. Bos/eksik alanlar
 * sessizce atlanir, cagiran tarafin null kontrolu yapmasi gerekmesin diye.
 */
export function aramaEslesiyor(
  alanlar: Array<string | null | undefined>,
  sorgu: string,
): boolean {
  const q = sorgu.trim().toLocaleLowerCase("tr");
  if (!q) {
    return true;
  }
  return alanlar.some((alan) => alan?.toLocaleLowerCase("tr").includes(q));
}
