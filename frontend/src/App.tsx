
import { Navigate, Route, Routes } from "react-router-dom";
import { rolAnaSayfasi, useAuth } from "./auth/AuthContext";
import { RequireRole } from "./auth/RequireRole";
import { DepartmanlarPage } from "./pages/admin/DepartmanlarPage";
import { DonemlerPage } from "./pages/admin/DonemlerPage";
import { IstatistiklerPage } from "./pages/admin/IstatistiklerPage";
import { SablonlarPage } from "./pages/admin/SablonlarPage";
import { SorumlularPage } from "./pages/admin/SorumlularPage";
import { StajlarPage } from "./pages/admin/StajlarPage";
import { StajyerlerPage } from "./pages/admin/StajyerlerPage";
import { TaleplerPage } from "./pages/admin/TaleplerPage";
import { YaklasanBitislerPage } from "./pages/admin/YaklasanBitislerPage";
import { LoginPage } from "./pages/LoginPage";
import { MePage } from "./pages/me/MePage";
import { SifreDegistirPage } from "./pages/SifreDegistirPage";
import { BtTalepleriPage } from "./pages/supervisor/BtTalepleriPage";
import { InternDetailPage } from "./pages/supervisor/InternDetailPage";
import { SupervisorHomePage } from "./pages/supervisor/SupervisorHomePage";
import { Layout } from "./ui/Layout";

/** "/" tek basina bir sayfa degil — kim girdiyse onun paneline yonlendirir. */
function AnaSayfayaGit() {
  const { oturum } = useAuth();
  return <Navigate to={oturum ? rolAnaSayfasi(oturum.rol) : "/login"} replace />;
}

export function App() {
  return (
    <Routes>
      {/* Token gerektirmeyen ekranlar (backend'de de /api/auth/** permitAll). */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/sifre-degistir" element={<SifreDegistirPage />} />

      <Route element={<Layout />}>
        <Route element={<RequireRole roller={["ADMIN"]} />}>
          {/* Sekmeler sol panele tasindi; bu rota artik yalnizca gruplama. */}
          <Route path="/admin">
            <Route index element={<Navigate to="/admin/departmanlar" replace />} />
            <Route path="departmanlar" element={<DepartmanlarPage />} />
            <Route path="donemler" element={<DonemlerPage />} />
            <Route path="kullanicilar">
              <Route index element={<Navigate to="/admin/kullanicilar/sorumlular" replace />} />
              <Route path="sorumlular" element={<SorumlularPage />} />
              <Route path="stajyerler" element={<StajyerlerPage />} />
            </Route>
            <Route path="stajlar" element={<StajlarPage />} />
            <Route path="sablonlar" element={<SablonlarPage />} />
            <Route path="yaklasan-bitisler" element={<YaklasanBitislerPage />} />
            <Route path="istatistikler" element={<IstatistiklerPage />} />
            <Route path="talepler" element={<TaleplerPage />} />
          </Route>
        </Route>

        <Route element={<RequireRole roller={["SORUMLU"]} />}>
          <Route path="/supervisor" element={<SupervisorHomePage />} />
          <Route path="/supervisor/bt-talepler" element={<BtTalepleriPage />} />
          <Route path="/supervisor/interns/:hashId" element={<InternDetailPage />} />
        </Route>

        <Route element={<RequireRole roller={["STAJYER"]} />}>
          <Route path="/me" element={<MePage />} />
        </Route>
      </Route>

      <Route path="/" element={<AnaSayfayaGit />} />
      {/* Bilinmeyen adres: oturum varsa kendi paneline, yoksa login'e. */}
      <Route path="*" element={<AnaSayfayaGit />} />
    </Routes>
  );
}