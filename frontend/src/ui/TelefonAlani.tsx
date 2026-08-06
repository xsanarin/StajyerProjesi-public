import { TextField } from "@mui/material";

const TELEFON_IPUCU = "10 haneli telefon numarası girin (örn. 5001234567).";

/** Rakam disi her karakter atilir: "(0500) 123 45 67" -> "05001234567". */
export function telefonHaneleri(deger: string): string {
  return deger.replace(/\D/g, "");
}

/**
 * 10 hane (5001234567) veya basinda 0 ile 11 hane (05001234567) kabul edilir.
 * Bos deger gecerli sayilir — telefon zorunlu alan degil.
 */
export function telefonGecerli(deger: string): boolean {
  const haneler = telefonHaneleri(deger);
  if (haneler === "") return true;
  return haneler.length === 10 || (haneler.length === 11 && haneler.startsWith("0"));
}

type Props = {
  deger: string;
  onDegis: (deger: string) => void;
};

export function TelefonAlani({ deger, onDegis }: Props) {
  return (
    <TextField
      label="Telefon"
      value={deger}
      onChange={(e) => onDegis(e.target.value)}
      error={!telefonGecerli(deger)}
      helperText={TELEFON_IPUCU}
      inputMode="tel"
      fullWidth
    />
  );
}
