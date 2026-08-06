
import DarkModeIcon from "@mui/icons-material/DarkMode";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import LightModeIcon from "@mui/icons-material/LightMode";
import LogoutIcon from "@mui/icons-material/Logout";
import {
  Avatar,
  Box,
  Collapse,
  Container,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
  Tooltip,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import type { Rol } from "../api/types";
import { useAuth } from "../auth/AuthContext";
import { useTema } from "./TemaProvider";

const PANEL_GENISLIGI = 240;

/** Panelin dibinde ham rol kodu yerine okunur etiket gosteriliyor. */
const rolEtiketi: Record<Rol, string> = {
  ADMIN: "Admin",
  SORUMLU: "Sorumlu",
  STAJYER: "Stajyer",
};

/**
 * Sol panelin menusu role gore degisir. Rota korumasi RequireRole'de —
 * burasi yalnizca gorunurluk, yetki kapisi degil.
 */
type MenuOgesi = {
  yol: string;
  etiket: string;
  /** Varsa alt menu; ust oge tiklanabilir kalir, kendi index rotasina gider. */
  alt?: { yol: string; etiket: string }[];
};

const menuler: Record<Rol, MenuOgesi[]> = {
  ADMIN: [
    { yol: "/admin/departmanlar", etiket: "Departmanlar" },
    { yol: "/admin/donemler", etiket: "Dönemler" },
    {
      yol: "/admin/kullanicilar",
      etiket: "Kullanıcılar",
      alt: [
        { yol: "/admin/kullanicilar/sorumlular", etiket: "Sorumlular" },
        { yol: "/admin/kullanicilar/stajyerler", etiket: "Stajyerler" },
      ],
    },
    { yol: "/admin/stajlar", etiket: "Stajlar" },
    { yol: "/admin/sablonlar", etiket: "Şablonlar" },
    { yol: "/admin/yaklasan-bitisler", etiket: "Yaklaşan Bitişler" },
    // Etiket sayfanin kendi basligiyla ayni: sayfada tek rapor var (departmana gore
    // stajyer sayisi), "Istatistikler" birden cok rapor vaat ediyordu.
    { yol: "/admin/istatistikler", etiket: "Stajyer Dağılımı" },
    { yol: "/admin/talepler", etiket: "BT Talepleri" },
  ],
  SORUMLU: [
    { yol: "/supervisor", etiket: "Stajyerlerim" },
    { yol: "/supervisor/bt-talepler", etiket: "BT Talepleri" },
  ],
  STAJYER: [{ yol: "/me", etiket: "Stajım" }],
};

/** Giris yapmis tum sayfalarin ortak cercevesi: sol panel + icerik alani. */
export function Layout() {
  const { oturum, oturumKapat } = useAuth();
  const { mod, modDegistir } = useTema();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  function cikis() {
    oturumKapat();
    navigate("/login", { replace: true });
  }

  const menu = oturum ? menuler[oturum.rol] : [];

  /**
   * Hangi alt menuler acik. Ilk deger adresten kuruluyor ki dogrudan
   * /admin/kullanicilar/stajyerler ile girildiginde menu acik gelsin; sonrasinda
   * kullanicinin actigi/kapattigi durum korunur.
   */
  const [acikYollar, setAcikYollar] = useState<string[]>(() =>
    menu.filter((m) => m.alt && pathname.startsWith(m.yol)).map((m) => m.yol),
  );

  function menuDegistir(yol: string) {
    setAcikYollar((y) => (y.includes(yol) ? y.filter((x) => x !== yol) : [...y, yol]));
  }

  /**
   * Secili menu: adrese uyan EN UZUN yol. Duz startsWith yetmiyor, cunku menude
   * ic ice yollar var: /supervisor/bt-talepler adresinde hem "BT Talepleri" hem
   * "Stajyerlerim" (/supervisor) eslesir ve ikisi birden secili gorunurdu.
   * En uzun eslesme, /supervisor/interns/:hashId gibi menude karsiligi olmayan
   * alt sayfalarda ust girisin (Stajyerlerim) secili kalmasini da korur.
   */
  const aktifYol = menu
    .filter((m) => !m.alt && pathname.startsWith(m.yol))
    .reduce<string | null>((enUzun, m) => (enUzun && enUzun.length >= m.yol.length ? enUzun : m.yol), null);

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
      <Drawer
        variant="permanent"
        sx={{
          width: PANEL_GENISLIGI,
          flexShrink: 0,
          "& .MuiDrawer-paper": {
            width: PANEL_GENISLIGI,
            boxSizing: "border-box",
            // Uzun e-posta/etiketler panelin sag kenarini asip sayfa zeminiyle
            // arada tasma cizgisi olusturmasin.
            overflowX: "hidden",
            // Ayirici cizgi acikca ve tam yukseklikte veriliyor: panel ile kart
            // ayni yuzey renginde oldugu icin, kenarlik yalnizca kartin bittigi
            // yere kadar gorunurse cizgi yarida kesilmis gibi duruyordu.
            height: "100%",
            borderRight: 1,
            borderColor: "divider",
          },
        }}
      >
        {/* Logo panelin sol ust kosesinde. Zemin beyaz oldugu icin mavi logo
            dogrudan okunuyor. "/" kendi rolunun ana sayfasina yonlendirir. */}
        <Box
          component={Link}
          to="/"
          aria-label="Ana sayfa"
          sx={{
            display: "flex",
            alignItems: "center",
            gap: 1.5,
            px: 2,
            py: 1.5,
            textDecoration: "none",
            // Koyu temada primary (#2563eb) koyu zeminde okunmuyor; baslik
            // metin rengine donuyor, acik temada mavi kaliyor.
            color: mod === "dark" ? "text.primary" : "primary.main",
          }}
        >
          <Typography variant="subtitle1" fontWeight={600} lineHeight={1.2}>
            Staj Envanter
          </Typography>
        </Box>
        <Divider />

        <List sx={{ py: 1 }}>
          {menu.map((m) => {
            const acik = acikYollar.includes(m.yol);
            return (
              <Box key={m.yol}>
                {/* Alt menusu olan oge yonlendirmez, yalnizca acar/kapatir —
                    yoksa tiklamak hep alt sayfaya gidip menuyu kapatilamaz yapardi.
                    Alt yollarda da (/supervisor/interns/:hashId) ust menu secili kalsin. */}
                <ListItemButton
                  {...(m.alt
                    ? { onClick: () => menuDegistir(m.yol) }
                    : { component: Link, to: m.yol })}
                  // Alt menusu olanda secili gorunumu alt ogeye birakiyoruz,
                  // yoksa iki satir ayni anda vurgulanmis olurdu.
                  selected={!m.alt && m.yol === aktifYol}
                >
                  <ListItemText primary={m.etiket} />
                  {m.alt && (acik ? <ExpandLessIcon /> : <ExpandMoreIcon />)}
                </ListItemButton>
                {m.alt && (
                  <Collapse in={acik} unmountOnExit>
                    <List disablePadding>
                      {m.alt.map((a) => (
                        <ListItemButton
                          key={a.yol}
                          component={Link}
                          to={a.yol}
                          selected={pathname.startsWith(a.yol)}
                          // Ikonlar kalkinca ust satirin metni 16px'e geldi;
                          // 7 (56px) girinti artik ust menuyle bagini koparmisti.
                          sx={{ pl: 4 }}
                        >
                          <ListItemText primary={a.etiket} />
                        </ListItemButton>
                      ))}
                    </List>
                  </Collapse>
                )}
              </Box>
            );
          })}
        </List>

        {/* Kim oldugumuz ve cikis panelin dibine yapisir. E-posta artik yazi olarak
            durmuyor; avatarin uzerine gelince ipucunda cikiyor. */}
        <Box sx={{ flexGrow: 1 }} />
        <Divider />
        {/* p:2 ustteki menu ogeleriyle ayni 16px'lik ic bosluk — avatar, menu
            ikonlariyla ayni dikeyde hizalaniyor. */}
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            gap: 1,
            width: "100%",
            boxSizing: "border-box",
            p: 2,
          }}
        >
          {/* Avatar + rol tek grup; minWidth:0 olmadan noWrap metin kucultemez
              ve grubu panel disina iterdi. */}
          <Box sx={{ display: "flex", alignItems: "center", gap: 1.5, minWidth: 0 }}>
            <Tooltip title={oturum?.email ?? ""}>
              <Avatar sx={{ width: 36, height: 36, bgcolor: "primary.main", fontSize: 16 }}>
                {oturum?.email?.[0]?.toUpperCase() ?? "?"}
              </Avatar>
            </Tooltip>
            <Typography variant="body2" fontWeight={600} noWrap>
              {oturum ? rolEtiketi[oturum.rol] : ""}
            </Typography>
          </Box>
          {/* Iki ikon kendi grubunda: disaridaki gap her cocuk arasina uygulandigi
              icin ikonlar da birbirinden ayriliyordu. */}
          <Box sx={{ display: "flex", flexShrink: 0 }}>
            <Tooltip title={mod === "light" ? "Koyu temaya geç" : "Açık temaya geç"}>
              <IconButton onClick={modDegistir} aria-label="Temayı değiştir" size="small">
                {mod === "light" ? (
                  <DarkModeIcon fontSize="small" />
                ) : (
                  <LightModeIcon fontSize="small" />
                )}
              </IconButton>
            </Tooltip>
            <Tooltip title="Çıkış">
              <IconButton onClick={cikis} aria-label="Çıkış" size="small">
                <LogoutIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Box>
        </Box>
      </Drawer>

      {/* minWidth:0 olmadan genis tablolar paneli iterek sayfayi yatay kaydirtir. */}
      <Box component="main" sx={{ flexGrow: 1, minWidth: 0 }}>
        <Container maxWidth="lg" sx={{ py: 3 }}>
          <Outlet />
        </Container>
      </Box>
    </Box>
  );
}
