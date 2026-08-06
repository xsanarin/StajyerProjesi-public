import {
  Alert,
  Button,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import type { ReactNode } from "react";
import type { DevamDurumu, DevamOzetResponse, DevamResponse } from "../api/types";
import { TabloIskeleti, useGecikmeliGoster } from "./TabloIskeleti";

/**
 * Aylik devam takvimi — sorumlu ekraninda da stajyer ekraninda da ayni goruntu.
 * Yalnizca gosterim yapar; veri cekme ve giris/cikis islemleri cagirana ait.
 *
 * Backend ayin HER gunu icin bir satir doner (kaydi olmayan gunler dahil), bu yuzden
 * burada gun uretmiyoruz — gelen listeyi oldugu gibi basiyoruz.
 */

const DURUM_GORUNUM: Record<DevamDurumu, { etiket: string; renk: "success" | "warning" | "error" | "default" }> = {
  TAM: { etiket: "Tam", renk: "success" },
  CIKIS_YOK: { etiket: "Çıkış yok", renk: "warning" },
  // Stajyer bildirdi, sorumlu onaylamadi: devama sayilmiyor, o yuzden yesil degil.
  ONAY_BEKLIYOR: { etiket: "Onay bekliyor", renk: "warning" },
  GELMEDI: { etiket: "Gelmedi", renk: "error" },
  BEKLENIYOR: { etiket: "Bekleniyor", renk: "default" },
  PLANSIZ: { etiket: "Plan dışı", renk: "default" },
};

/**
 * girisSaati/cikisSaati backend'de LocalTime DEGIL LocalDateTime — "2026-07-27T09:15:00"
 * gibi gelir. Takvimde tarih zaten ayri sutunda oldugu icin sadece saati gosteriyoruz.
 */
export function saatGoster(isoZaman: string | null): string {
  if (!isoZaman) {
    return "-";
  }
  const parca = isoZaman.split("T")[1];
  return parca ? parca.slice(0, 5) : isoZaman;
}

const AY_ADLARI = [
  "Ocak",
  "Şubat",
  "Mart",
  "Nisan",
  "Mayıs",
  "Haziran",
  "Temmuz",
  "Ağustos",
  "Eylül",
  "Ekim",
  "Kasım",
  "Aralık",
];

export function AyGezgini({
  yil,
  ay,
  onDegis,
}: {
  yil: number;
  /** 1-12. Backend de 1-tabanli bekliyor; JS'in 0-tabanli ayi buraya hic girmiyor. */
  ay: number;
  onDegis: (yil: number, ay: number) => void;
}) {
  function kaydir(fark: number) {
    // Ay 1'den geriye / 12'den ileriye tasarsa yili da cevir.
    const toplam = (yil * 12 + (ay - 1)) + fark;
    onDegis(Math.floor(toplam / 12), (toplam % 12) + 1);
  }

  return (
    <Stack direction="row" alignItems="center" spacing={1}>
      <Button size="small" onClick={() => kaydir(-1)}>
        {"< Önceki"}
      </Button>
      <Typography variant="subtitle1" sx={{ minWidth: 140, textAlign: "center" }}>
        {AY_ADLARI[ay - 1]} {yil}
      </Typography>
      <Button size="small" onClick={() => kaydir(1)}>
        {"Sonraki >"}
      </Button>
    </Stack>
  );
}

export function DevamOzetSatiri({ ozet }: { ozet: DevamOzetResponse }) {
  return (
    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
      <Chip size="small" label={`Planlı gün: ${ozet.planliGun}`} />
      <Chip size="small" color="success" label={`Gelinen: ${ozet.gelinenGun}`} />
      <Chip size="small" color="error" label={`Devamsız: ${ozet.devamsizGun}`} />
      {/* Devamsiz icinde sayiliyor; onaylanirsa gelinen'e gecer. Sifirsa gostermiyoruz. */}
      {ozet.onayBekleyenGun > 0 && (
        <Chip size="small" color="warning" label={`Onay bekleyen: ${ozet.onayBekleyenGun}`} />
      )}
      <Chip size="small" label={`Plan dışı: ${ozet.planDisiGun}`} />
      <Chip size="small" color="primary" label={`Devam: %${ozet.devamYuzdesi}`} />
    </Stack>
  );
}

export function DevamTakvimi({
  gunler,
  yukleniyor,
  /** Plan disi gunler cogunlukla gurultu (hafta sonu); varsayilan olarak gizli. */
  planDisiGoster = false,
  /**
   * Satir basina islem hucresi (admin duzeltme/silme). Verilmezse sutun hic basilmaz —
   * sorumlu ve stajyer ekranlari bos bir sutun gormesin.
   */
  satirIslemleri,
  /**
   * Stajin baslangic tarihi. null ise takvim BOS cikar: tarih bilinmeden hangi
   * gunun planli oldugu hesaplanamaz. Bunu sessizce bos gostermek yerine
   * sebebini yaziyoruz.
   */
  stajBaslangicTarihi,
}: {
  gunler: DevamResponse[] | undefined;
  yukleniyor: boolean;
  planDisiGoster?: boolean;
  satirIslemleri?: (gun: DevamResponse) => ReactNode;
  stajBaslangicTarihi?: string | null;
}) {
  const gosterilecek = (gunler ?? []).filter((g) => planDisiGoster || g.durum !== "PLANSIZ");
  const iskeletGoster = useGecikmeliGoster(yukleniyor);
  const sutunSayisi = satirIslemleri ? 6 : 5;
  const tarihEksik = stajBaslangicTarihi === null;

  /**
   * "Basi devamsiz, sonrasi dolu" kalibi: bu ayda ilk devam kaydindan ONCEKI
   * gunlerin hepsi devamsiz.
   *
   * Neredeyse her zaman stajin baslangic tarihinin yanlis olmasindan gelir:
   * staj acilirken tarih bos birakilinca backend DONEMIN tarihini yaziyor, o da
   * cogunlukla gecmiste. Stajyer 28'inde basladigi halde staj 1'inde baslamis
   * gorunuyor ve aradaki gunler hakli olarak "Gelmedi" cikiyor. Takvim yalan
   * soylemiyor ama sebebi ekranda yazmayinca hata gibi okunuyor.
   */
  const ilkKayit = (gunler ?? []).find((g) => g.hashId);
  const oncesiHepDevamsiz =
    ilkKayit !== undefined &&
    (gunler ?? []).some((g) => g.tarih < ilkKayit.tarih && g.durum === "GELMEDI") &&
    (gunler ?? [])
      .filter((g) => g.tarih < ilkKayit.tarih && g.planli)
      .every((g) => g.durum === "GELMEDI");

  return (
    <>
    {tarihEksik && (
      <Alert severity="warning" sx={{ mb: 2 }}>
        Bu stajın başlangıç tarihi girilmemiş. Tarih olmadan hangi günlerin planlı
        olduğu hesaplanamaz, bu yüzden takvim boş görünür ve devamsızlık sayılmaz.
        Stajlar ekranından tarihleri girin.
      </Alert>
    )}
    {oncesiHepDevamsiz && (
      <Alert severity="info" sx={{ mb: 2 }}>
        İlk devam kaydı <strong>{ilkKayit?.tarih}</strong>, ama staj{" "}
        <strong>{stajBaslangicTarihi ?? "daha erken"}</strong> tarihinde başlamış görünüyor;
        arada kalan planlı günler devamsız sayılıyor. Stajyer gerçekten {ilkKayit?.tarih}{" "}
        tarihinde başladıysa başlangıç tarihi yanlış — Stajlar ekranında ilgili satırın
        menüsünden <strong>Düzenle</strong> ile düzeltirseniz bu günler devamsızlıktan çıkar.
      </Alert>
    )}
    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Tarih</TableCell>
            <TableCell>Giriş</TableCell>
            <TableCell>Çıkış</TableCell>
            <TableCell>Durum</TableCell>
            <TableCell>Kayıt</TableCell>
            {satirIslemleri && <TableCell align="right" width={56} />}
          </TableRow>
        </TableHead>
        <TableBody>
          {iskeletGoster && <TabloIskeleti sutun={sutunSayisi} satir={8} />}
          {!yukleniyor && gosterilecek.length === 0 && (
            <TableRow>
              <TableCell colSpan={sutunSayisi}>Bu ayda gösterilecek gün yok.</TableCell>
            </TableRow>
          )}
          {gosterilecek.map((g) => {
            const gorunum = DURUM_GORUNUM[g.durum];
            return (
              <TableRow key={g.tarih} hover>
                <TableCell>{g.tarih}</TableCell>
                <TableCell>{saatGoster(g.girisSaati)}</TableCell>
                <TableCell>{saatGoster(g.cikisSaati)}</TableCell>
                <TableCell>
                  <Chip size="small" label={gorunum.etiket} color={gorunum.renk} />
                </TableCell>
                <TableCell>{g.manuelMi ? "Manuel" : g.hashId ? "Otomatik" : "-"}</TableCell>
                {satirIslemleri && <TableCell align="right">{satirIslemleri(g)}</TableCell>}
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </TableContainer>
    </>
  );
}
