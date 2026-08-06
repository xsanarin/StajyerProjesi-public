import { IconButton, Menu, MenuItem } from "@mui/material";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import { useState } from "react";

export type SatirIslemi = {
  etiket: string;
  onSecim: () => void;
  /** Silme gibi geri alinamayan islemler kirmizi. */
  tehlikeli?: boolean;
  /** Duruma gore anlamsiz olan islem menude hic gorunmesin (bkz. StajlarPage). */
  gizli?: boolean;
};

/**
 * Tablo satirinin sonundaki "⋮" islem menusu. Yan yana buton dizmek yerine:
 * satir sayisi arttikca butonlar tabloyu tasiyor ve yanlis tiklama kolaylasiyor.
 */
export function SatirMenusu({ islemler }: { islemler: SatirIslemi[] }) {
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);
  const gorunenler = islemler.filter((i) => !i.gizli);

  if (gorunenler.length === 0) {
    return null;
  }

  return (
    <>
      <IconButton size="small" onClick={(e) => setAnchor(e.currentTarget)}>
        <MoreVertIcon fontSize="small" />
      </IconButton>
      <Menu
        open={anchor !== null}
        anchorEl={anchor}
        onClose={() => setAnchor(null)}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
        transformOrigin={{ vertical: "top", horizontal: "right" }}
      >
        {gorunenler.map((i) => (
          <MenuItem
            key={i.etiket}
            onClick={() => {
              setAnchor(null);
              i.onSecim();
            }}
            sx={i.tehlikeli ? { color: "error.main" } : undefined}
          >
            {i.etiket}
          </MenuItem>
        ))}
      </Menu>
    </>
  );
}
