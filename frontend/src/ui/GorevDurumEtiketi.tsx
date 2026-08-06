import { Chip } from "@mui/material";
import DoneIcon from "@mui/icons-material/Done";
import HighlightOffIcon from "@mui/icons-material/HighlightOff";
import RadioButtonUncheckedIcon from "@mui/icons-material/RadioButtonUnchecked";
import TaskAltIcon from "@mui/icons-material/TaskAlt";
import type { ReactElement } from "react";
import type { GorevDurumu } from "../api/types";

type Bicim = {
  etiket: string;
  renk: "default" | "info" | "success" | "warning";
  ikon: ReactElement;
};

/**
 * Gorev/madde durumu tek yerden: enum adi ("REDDEDILDI") kullaniciya gosterilmez.
 * Dolgu yerine outlined — bir listede onlarca rozet yan yana geliyor, dolgu
 * olanlar metnin onune geciyordu.
 */
const BICIM: Record<GorevDurumu, Bicim> = {
  BEKLIYOR: { etiket: "Bekliyor", renk: "default", ikon: <RadioButtonUncheckedIcon /> },
  TAMAMLANDI: { etiket: "Tamamlandı", renk: "info", ikon: <DoneIcon /> },
  ONAYLANDI: { etiket: "Onaylandı", renk: "success", ikon: <TaskAltIcon /> },
  // Ret bir hata degil, "tekrar yap" demek — kirmizi yerine turuncu.
  REDDEDILDI: { etiket: "Reddedildi", renk: "warning", ikon: <HighlightOffIcon /> },
};

export function GorevDurumEtiketi({ durum }: { durum: GorevDurumu }) {
  const bicim = BICIM[durum];
  return (
    <Chip
      size="small"
      variant="outlined"
      color={bicim.renk}
      icon={bicim.ikon}
      label={bicim.etiket}
      sx={{ fontWeight: 500, flexShrink: 0, "& .MuiChip-icon": { fontSize: 16 } }}
    />
  );
}
