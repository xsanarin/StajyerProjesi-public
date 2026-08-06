import { Navigate, Outlet, useLocation } from "react-router-dom";
import { rolAnaSayfasi, useAuth } from "./AuthContext";
import type { Rol } from "../api/types";

/**
 * Rota kapisi. Iki ayri durum, iki ayri davranis:
 *   - oturum yok      -> /login (nereden geldigini state'te tasiyoruz, sonra geri donsun)
 *   - oturum var ama rol tutmuyor -> kendi ana sayfasina at; login'e atmak yanlis olurdu,
 *     kullanici zaten giris yapmis, sadece yanlis kapiyi zorluyor.
 *
 * Bu yalnizca UI kolayligi; asil yetki kontrolu backend'de (SecurityConfig + CurrentUser).
 */
export function RequireRole({ roller }: { roller: Rol[] }) {
  const { oturum } = useAuth();
  const konum = useLocation();

  if (!oturum) {
    return <Navigate to="/login" replace state={{ nereden: konum.pathname }} />;
  }
  if (!roller.includes(oturum.rol)) {
    return <Navigate to={rolAnaSayfasi(oturum.rol)} replace />;
  }
  return <Outlet />;
}
