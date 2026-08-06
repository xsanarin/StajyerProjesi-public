import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  ListItemText,
  MenuItem,
  OutlinedInput,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import {
  adminDegerlendirmeGetir,
  adminKilidiAc,
  adminPuanDuzelt,
  departmanlariGetir,
  donemleriGetir,
  sorumlulariGetir,
  stajDepartmanDegistir,
  stajGuncelle,
  stajKaydet,
  stajAra,
  stajSil,
  stajyerleriGetir,
  yedekSorumluAta,
} from "../../api/admin";
import { SEVIYELER } from "../../api/types";
import type { HaftaGunu, Seviye, StajResponse } from "../../api/types";
import { AramaKutusu } from "../../ui/AramaKutusu";
import { IslemButonu } from "../../ui/IslemButonu";
import { OnayDialog } from "../../ui/OnayDialog";
import { SatirMenusu } from "../../ui/SatirMenusu";
import { SayfaCubugu } from "../../ui/SayfaCubugu";
import { bayatVeriSx, TabloIskeleti, useGecikmeliGoster } from "../../ui/TabloIskeleti";
import { useToast } from "../../ui/ToastProvider";
import { useGecikmeliDeger } from "../../ui/useGecikmeliDeger";
import { StajDevamDialog } from "./StajDevamDialog";

const GUNLER: { deger: HaftaGunu; etiket: string }[] = [
  { deger: "MONDAY", etiket: "Pazartesi" },
  { deger: "TUESDAY", etiket: "Salı" },
  { deger: "WEDNESDAY", etiket: "Çarşamba" },
  { deger: "THURSDAY", etiket: "Perşembe" },
  { deger: "FRIDAY", etiket: "Cuma" },
  { deger: "SATURDAY", etiket: "Cumartesi" },
  { deger: "SUNDAY", etiket: "Pazar" },
];

type Form = {
  stajyerHashId: string;
  donemHashId: string;
  departmanHashId: string;
  asilSorumluHashId: string;
  yedekSorumluHashId: string;
  seviye: Seviye | "";
  stajBaslangicTarihi: string;
  stajBitisTarihi: string;
  devamGunleri: HaftaGunu[];
};

const bosForm: Form = {
  stajyerHashId: "",
  donemHashId: "",
  departmanHashId: "",
  asilSorumluHashId: "",
  yedekSorumluHashId: "",
  seviye: "",
  stajBaslangicTarihi: "",
  stajBitisTarihi: "",
  devamGunleri: ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
};

export function StajlarPage() {
  const toast = useToast();
  const qc = useQueryClient();

  const [arama, setArama] = useState("");
  const [sayfa, setSayfa] = useState(0);
  const [boyut, setBoyut] = useState(25);
  const gecikmeliArama = useGecikmeliDeger(arama);

  /**
   * Liste sunucudan sayfali geliyor: arama, sayfa ve boyut sorgu anahtarinin
   * parcasi. Anahtar ["stajlar", ...] ile basladigi icin mutasyonlardaki
   * invalidateQueries({ queryKey: ["stajlar"] }) hepsini kapsamaya devam eder.
   *
   * placeholderData: sayfa degistirirken tablo bosalip zipladigi icin onceki
   * sayfa yeni veri gelene kadar ekranda kaliyor.
   */
  const stajlar = useQuery({
    queryKey: ["stajlar", gecikmeliArama, sayfa, boyut],
    queryFn: () => stajAra(gecikmeliArama, sayfa, boyut),
    placeholderData: (onceki) => onceki,
  });
  const stajyerler = useQuery({ queryKey: ["stajyerler"], queryFn: stajyerleriGetir });
  const donemler = useQuery({ queryKey: ["donemler"], queryFn: donemleriGetir });
  const departmanlar = useQuery({ queryKey: ["departmanlar"], queryFn: departmanlariGetir });
  const sorumlular = useQuery({ queryKey: ["sorumlular"], queryFn: sorumlulariGetir });

  const [form, setForm] = useState<Form | null>(null);
  /** Acilmis stajin duzeltilmesi: yalnizca tarih, devam plani ve asil sorumlu degisir. */
  const [duzenlenen, setDuzenlenen] = useState<{
    staj: StajResponse;
    stajBaslangicTarihi: string;
    stajBitisTarihi: string;
    devamGunleri: HaftaGunu[];
    asilSorumluHashId: string;
  } | null>(null);
  const [silinecek, setSilinecek] = useState<StajResponse | null>(null);
  const [devamStaj, setDevamStaj] = useState<StajResponse | null>(null);
  const [departmanDegisimi, setDepartmanDegisimi] = useState<{
    staj: StajResponse;
    departmanHashId: string;
    asilSorumluHashId: string;
    yedekSorumluHashId: string;
  } | null>(null);
  const [yedekAtama, setYedekAtama] = useState<{ staj: StajResponse; sorumluHashId: string } | null>(
    null,
  );
  const [puanDuzeltme, setPuanDuzeltme] = useState<{
    staj: StajResponse;
    puan: string;
    yorum: string;
    gerekce: string;
    /** Mevcut degerlendirme bir kez forma yazildi mi? Yazdiktan sonra kullanicinin
        girdisinin ustune tekrar yazmamak icin. */
    dolduruldu: boolean;
  } | null>(null);
  const [kilitAcilacak, setKilitAcilacak] = useState<StajResponse | null>(null);

  const gorunenStajlar = stajlar.data?.icerik ?? [];
  const toplamKayit = stajlar.data?.toplamKayit ?? 0;

  /**
   * Yeni aramada ilk sayfaya don. Yoksa 5. sayfadayken arama yapinca
   * sonuclar tek sayfaya siginir ve kullanici bos tablo gorur.
   */
  useEffect(() => {
    setSayfa(0);
  }, [gecikmeliArama]);

  const iskeletGoster = useGecikmeliGoster(stajlar.isPending);

  // Duzeltme dialogu acilinca mevcut puani cekip forma on-doldur.
  const mevcutDegerlendirme = useQuery({
    queryKey: ["degerlendirme", puanDuzeltme?.staj.hashId],
    queryFn: () => adminDegerlendirmeGetir(puanDuzeltme!.staj.hashId),
    enabled: puanDuzeltme !== null,
  });

  useEffect(() => {
    const d = mevcutDegerlendirme.data;
    if (!d) {
      return;
    }
    setPuanDuzeltme((p) =>
      p && !p.dolduruldu && p.staj.hashId === d.stajHashId
        ? {
            ...p,
            puan: d.puan != null ? String(d.puan) : "",
            yorum: d.yorum ?? "",
            dolduruldu: true,
          }
        : p,
    );
  }, [mevcutDegerlendirme.data]);

  /**
   * Sorumlu listesi departmana gore daralir: bir staj hangi departmandaysa asil ve yedek
   * o departmanin sorumlularindan secilir. Departmani olmayan sorumlular listede cikmaz —
   * once onlara departman atanmali.
   */
  /**
   * Donem secilince staj tarihlerini ON-DOLDURUR.
   *
   * NEDEN: tarih alani bos birakilirsa backend sessizce DONEMIN tarihini
   * yaziyor (StajServiceImpl: donem.getBaslangicTarihi()). 29 Temmuz'da acilan
   * staj boylece 15 Haziran'da baslamis oluyor ve aradaki tum plan gunleri
   * "Gelmedi" cikiyor — kimse ne kaydedildigini gormedigi icin fark edilmiyordu.
   * Artik ne kaydedilecegi formda yaziyor ve degistirilebiliyor.
   *
   * Baslangic GECMISE gitmez: donem baslangici ile bugunun gec olani secilir.
   * Donem ortasinda ise alinan stajyer icin dogru olan bugundur; donem basinda
   * toplu giris yapiliyorsa zaten donem tarihi bugunden ileridedir.
   */
  const donemTarihleri = (donemHashId: string) => {
    const donem = (donemler.data ?? []).find((d) => d.hashId === donemHashId);
    const bugun = new Date().toISOString().slice(0, 10);
    const baslangic = donem?.baslangicTarihi
      ? donem.baslangicTarihi > bugun
        ? donem.baslangicTarihi
        : bugun
      : "";
    return {
      donemHashId,
      stajBaslangicTarihi: baslangic,
      stajBitisTarihi: donem?.bitisTarihi ?? "",
    };
  };

  const departmanSorumlulari = useMemo(() => {
    const hepsi = sorumlular.data ?? [];
    if (!form?.departmanHashId) {
      return [];
    }
    return hepsi.filter((s) => s.departmanHashId === form.departmanHashId);
  }, [sorumlular.data, form?.departmanHashId]);

  const kaydet = useMutation({
    mutationFn: (f: Form) =>
      stajKaydet({
        stajyerHashId: f.stajyerHashId,
        donemHashId: f.donemHashId,
        departmanHashId: f.departmanHashId,
        asilSorumluHashId: f.asilSorumluHashId,
        yedekSorumluHashId: f.yedekSorumluHashId || null,
        seviye: f.seviye || null,
        stajBaslangicTarihi: f.stajBaslangicTarihi || null,
        stajBitisTarihi: f.stajBitisTarihi || null,
        devamGunleri: f.devamGunleri.length ? f.devamGunleri : null,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setForm(null);
      toast.basari("Staj oluşturuldu.");
    },
    onError: (err) => toast.hata(err, "Staj oluşturulamadı"),
  });

  const departmanDegistir = useMutation({
    mutationFn: (d: NonNullable<typeof departmanDegisimi>) =>
      stajDepartmanDegistir(d.staj.hashId, {
        departmanHashId: d.departmanHashId,
        asilSorumluHashId: d.asilSorumluHashId,
        // Bos secim = yedek yok; backend null bekliyor.
        yedekSorumluHashId: d.yedekSorumluHashId || null,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setDepartmanDegisimi(null);
      toast.basari("Stajın departmanı değiştirildi.");
    },
    onError: (err) => toast.hata(err, "Departman değiştirilemedi"),
  });

  const duzelt = useMutation({
    mutationFn: (d: NonNullable<typeof duzenlenen>) =>
      stajGuncelle(d.staj.hashId, {
        stajBaslangicTarihi: d.stajBaslangicTarihi || null,
        stajBitisTarihi: d.stajBitisTarihi || null,
        devamGunleri: d.devamGunleri,
        asilSorumluHashId: d.asilSorumluHashId || null,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setDuzenlenen(null);
      toast.basari("Staj güncellendi.");
    },
    onError: (err) => toast.hata(err, "Staj güncellenemedi"),
  });

  const yedekAta = useMutation({
    mutationFn: (d: { stajHashId: string; sorumluHashId: string }) =>
      yedekSorumluAta(d.stajHashId, d.sorumluHashId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setYedekAtama(null);
      toast.basari("Yedek sorumlu atandı.");
    },
    onError: (err) => toast.hata(err, "Yedek sorumlu atanamadı"),
  });

  const puanDuzelt = useMutation({
    mutationFn: (d: { stajHashId: string; puan: number; yorum: string; gerekce: string }) =>
      adminPuanDuzelt(d.stajHashId, {
        puan: d.puan,
        yorum: d.yorum || null,
        gerekce: d.gerekce,
      }),
    onSuccess: (d) => {
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      qc.invalidateQueries({ queryKey: ["degerlendirme"] });
      setPuanDuzeltme(null);
      toast.basari(`Puan düzeltildi: ${d.puan}`);
    },
    onError: (err) => toast.hata(err, "Puan düzeltilemedi"),
  });

  const kilidiAc = useMutation({
    mutationFn: (stajHashId: string) => adminKilidiAc(stajHashId),
    onSuccess: () => {
      // Kilit acilinca staj yeniden yazilabilir hale gelir; sorumlunun listesi de degisir.
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setKilitAcilacak(null);
      toast.basari("Kilit açıldı, sorumlu yeniden puanlayabilir.");
    },
    onError: (err) => toast.hata(err, "Kilit açılamadı"),
  });

  const sil = useMutation({
    mutationFn: stajSil,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["stajlar"] });
      setSilinecek(null);
      toast.basari("Staj silindi.");
    },
    onError: (err) => toast.hata(err, "Staj silinemedi"),
  });

  // Yedek atama dialogu da ayni departman kuralina uyar, asil sorumlu haric.
  /**
   * Yedek adaylari: stajin departmanindaki sorumlular, o stajin asili haric.
   *
   * BASKA BIR STAJIN ASILI OLMAK ENGEL DEGIL — ayni kisi bir stajyerin asili,
   * digerinin yedegi olabilir. Backend de buna izin veriyor. Listede birini
   * goremiyorsan sebebi tek: DEPARTMANI farkli (ya da hic departmani yok).
   */
  const yedekAdaylari = (staj: StajResponse) =>
    (sorumlular.data ?? []).filter(
      (s) => s.departmanHashId === staj.departmanHashId && s.hashId !== staj.asilSorumluHashId,
    );

  /**
   * Secim kutusunun altinda "neden bu kadar az kisi var" sorusunu yanitlar.
   * Sessizce filtrelemek kullanicinin kafasini karistiriyordu: birinin listede
   * olmamasini "sistem onu kilitlemis" diye okumak cok kolay.
   */
  const sorumluIpucu = (departmanHashId: string | null, adaySayisi: number) => {
    if (!departmanHashId) {
      return "Önce departman seçin.";
    }
    const ad = (departmanlar.data ?? []).find((d) => d.hashId === departmanHashId)?.ad;
    if (adaySayisi === 0) {
      return `${ad ?? "Bu departman"} departmanında sorumlu yok.`;
    }
    return `Yalnızca ${ad ?? "seçili departman"} sorumluları listelenir; başka stajın asıl sorumlusu olmak engel değildir.`;
  };

  return (
    <>
      <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
          Stajlar
        </Typography>
        <AramaKutusu
          deger={arama}
          onDegis={setArama}
          placeholder="Stajyer, departman, sorumlu..."
          genislik={280}
        />
        <Button variant="contained" onClick={() => setForm(bosForm)}>
          Yeni Staj
        </Button>
      </Stack>

      <TableContainer component={Paper} sx={bayatVeriSx(stajlar.isPlaceholderData)}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Stajyer</TableCell>
              <TableCell>Dönem</TableCell>
              <TableCell>Departman</TableCell>
              <TableCell>Asıl sorumlu</TableCell>
              <TableCell>Yedek sorumlu</TableCell>
              <TableCell>Tarihler</TableCell>
              <TableCell>Durum</TableCell>
              <TableCell align="right" width={56} />
            </TableRow>
          </TableHead>
          <TableBody>
            {iskeletGoster && <TabloIskeleti sutun={8} />}
            {gorunenStajlar.map((s) => (
              <TableRow key={s.hashId} hover>
                <TableCell>{s.stajyerAdSoyad}</TableCell>
                <TableCell>{s.donemAd}</TableCell>
                <TableCell>{s.departmanAd}</TableCell>
                <TableCell>{s.asilSorumluAdSoyad ?? "-"}</TableCell>
                <TableCell>{s.yedekSorumluAdSoyad ?? "-"}</TableCell>
                {/* "2026-01-01 - 2027-06-30" dar sutunda uc satira boluniyordu. */}
                <TableCell sx={{ whiteSpace: "nowrap" }}>
                  {s.stajBaslangicTarihi ?? "?"} - {s.stajBitisTarihi ?? "?"}
                </TableCell>
                <TableCell>
                  <Chip
                    label={s.durum}
                    size="small"
                    color={s.durum === "DEVAM_EDIYOR" ? "primary" : "default"}
                  />
                </TableCell>
                <TableCell align="right">
                  <SatirMenusu
                    islemler={[
                      {
                        etiket: "Düzenle",
                        onSecim: () =>
                          setDuzenlenen({
                            staj: s,
                            stajBaslangicTarihi: s.stajBaslangicTarihi ?? "",
                            stajBitisTarihi: s.stajBitisTarihi ?? "",
                            devamGunleri: s.devamGunleri ?? [],
                            asilSorumluHashId: s.asilSorumluHashId ?? "",
                          }),
                        // Kapanmis staj gecmis veri; backend duzeltmeyi reddediyor.
                        gizli: s.durum !== "DEVAM_EDIYOR",
                      },
                      { etiket: "Devam Kayıtları", onSecim: () => setDevamStaj(s) },
                      {
                        etiket: "Departman Değiştir",
                        onSecim: () =>
                          setDepartmanDegisimi({
                            staj: s,
                            departmanHashId: s.departmanHashId ?? "",
                            asilSorumluHashId: "",
                            yedekSorumluHashId: "",
                          }),
                        gizli: s.durum !== "DEVAM_EDIYOR",
                      },
                      { etiket: "Yedek Ata", onSecim: () => setYedekAtama({ staj: s, sorumluHashId: "" }) },
                      // Puan duzeltme ve kilit acma yalnizca kapanmis stajda anlamli:
                      // devam eden stajda puan henuz yok, kilit de kapanmamis.
                      {
                        etiket: "Puan Düzelt",
                        onSecim: () =>
                          setPuanDuzeltme({
                            staj: s,
                            puan: "",
                            yorum: "",
                            gerekce: "",
                            dolduruldu: false,
                          }),
                        gizli: s.durum === "DEVAM_EDIYOR",
                      },
                      {
                        etiket: "Kilidi Aç",
                        onSecim: () => setKilitAcilacak(s),
                        gizli: s.durum === "DEVAM_EDIYOR",
                      },
                      { etiket: "Sil", onSecim: () => setSilinecek(s), tehlikeli: true },
                    ]}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <SayfaCubugu
          toplamKayit={toplamKayit}
          sayfa={sayfa}
          boyut={boyut}
          onSayfaDegis={setSayfa}
          onBoyutDegis={(yeni) => {
            // Boyut degisince mevcut sayfa numarasi anlamsizlasir (50'lik 3. sayfa
            // 10'luk listede baska kayitlara denk gelir) — basa donuyoruz.
            setBoyut(yeni);
            setSayfa(0);
          }}
        />
      </TableContainer>

      <StajDevamDialog staj={devamStaj} onKapat={() => setDevamStaj(null)} />

      {/* ---- Yeni staj ---- */}
      <Dialog open={form !== null} onClose={() => setForm(null)} maxWidth="sm" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (form) {
              kaydet.mutate(form);
            }
          }}
        >
          <DialogTitle>Yeni Staj</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                select
                label="Stajyer"
                value={form?.stajyerHashId ?? ""}
                onChange={(e) => setForm((f) => (f ? { ...f, stajyerHashId: e.target.value } : f))}
                required
                fullWidth
              >
                {stajyerler.data?.map((s) => (
                  <MenuItem key={s.hashId} value={s.hashId}>
                    {s.ad} {s.soyad}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                select
                label="Dönem"
                value={form?.donemHashId ?? ""}
                onChange={(e) => setForm((f) => (f ? { ...f, ...donemTarihleri(e.target.value) } : f))}
                required
                fullWidth
              >
                {donemler.data?.map((d) => (
                  <MenuItem key={d.hashId} value={d.hashId}>
                    {d.ad}
                    {d.aktif ? " (aktif)" : ""}
                  </MenuItem>
                ))}
              </TextField>

              {/* Departman degisince sorumlu secimleri gecersiz kalir; ikisini de sifirliyoruz. */}
              <TextField
                select
                label="Departman"
                value={form?.departmanHashId ?? ""}
                onChange={(e) =>
                  setForm((f) =>
                    f
                      ? {
                          ...f,
                          departmanHashId: e.target.value,
                          asilSorumluHashId: "",
                          yedekSorumluHashId: "",
                        }
                      : f,
                  )
                }
                required
                fullWidth
              >
                {departmanlar.data?.map((d) => (
                  <MenuItem key={d.hashId} value={d.hashId}>
                    {d.ad}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                select
                label="Asıl sorumlu"
                value={form?.asilSorumluHashId ?? ""}
                onChange={(e) =>
                  setForm((f) => (f ? { ...f, asilSorumluHashId: e.target.value } : f))
                }
                required
                disabled={!form?.departmanHashId}
                helperText={sorumluIpucu(form?.departmanHashId ?? null, departmanSorumlulari.length)}
                fullWidth
              >
                {departmanSorumlulari.map((s) => (
                  <MenuItem key={s.hashId} value={s.hashId}>
                    {s.ad} {s.soyad}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                select
                label="Yedek sorumlu"
                value={form?.yedekSorumluHashId ?? ""}
                onChange={(e) =>
                  setForm((f) => (f ? { ...f, yedekSorumluHashId: e.target.value } : f))
                }
                disabled={!form?.departmanHashId}
                helperText="Zorunlu değil; departmanda tek sorumlu varsa boş bırakın."
                fullWidth
              >
                <MenuItem value="">(yok)</MenuItem>
                {departmanSorumlulari
                  .filter((s) => s.hashId !== form?.asilSorumluHashId)
                  .map((s) => (
                    <MenuItem key={s.hashId} value={s.hashId}>
                      {s.ad} {s.soyad}
                    </MenuItem>
                  ))}
              </TextField>

              <TextField
                select
                label="Seviye"
                value={form?.seviye ?? ""}
                onChange={(e) =>
                  setForm((f) => (f ? { ...f, seviye: e.target.value as Seviye | "" } : f))
                }
                fullWidth
              >
                <MenuItem value="">(varsayılan)</MenuItem>
                {SEVIYELER.map((sv) => (
                  <MenuItem key={sv} value={sv}>
                    {sv}
                  </MenuItem>
                ))}
              </TextField>

              <Stack direction="row" spacing={2}>
                <TextField
                  label="Başlangıç"
                  type="date"
                  value={form?.stajBaslangicTarihi ?? ""}
                  onChange={(e) =>
                    setForm((f) => (f ? { ...f, stajBaslangicTarihi: e.target.value } : f))
                  }
                  InputLabelProps={{ shrink: true }}
                  helperText="Stajyerin ilk geldiği gün. Öncesi devamsız sayılmaz."
                  fullWidth
                />
                <TextField
                  label="Bitiş"
                  type="date"
                  value={form?.stajBitisTarihi ?? ""}
                  onChange={(e) =>
                    setForm((f) => (f ? { ...f, stajBitisTarihi: e.target.value } : f))
                  }
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                />
              </Stack>

              {/* Devam takvimi bu gunlere gore "planli/plansiz" hesaplar (DevamServiceImpl). */}
              <FormControl fullWidth>
                <InputLabel id="devam-gunleri-label">Devam günleri</InputLabel>
                <Select
                  labelId="devam-gunleri-label"
                  multiple
                  value={form?.devamGunleri ?? []}
                  onChange={(e) =>
                    setForm((f) =>
                      f ? { ...f, devamGunleri: e.target.value as HaftaGunu[] } : f,
                    )
                  }
                  input={<OutlinedInput label="Devam günleri" />}
                  renderValue={(secili) =>
                    GUNLER.filter((g) => (secili as HaftaGunu[]).includes(g.deger))
                      .map((g) => g.etiket)
                      .join(", ")
                  }
                >
                  {GUNLER.map((g) => (
                    <MenuItem key={g.deger} value={g.deger}>
                      <Checkbox checked={(form?.devamGunleri ?? []).includes(g.deger)} />
                      <ListItemText primary={g.etiket} />
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setForm(null)}>Vazgeç</Button>
            <IslemButonu type="submit" variant="contained" calisiyor={kaydet.isPending}>
              Oluştur
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      {/* ---- Departman degistirme ---- */}
      <Dialog
        open={departmanDegisimi !== null}
        onClose={() => setDepartmanDegisimi(null)}
        maxWidth="xs"
        fullWidth
      >
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (departmanDegisimi) {
              departmanDegistir.mutate(departmanDegisimi);
            }
          }}
        >
          <DialogTitle>Departman Değiştir</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {departmanDegisimi?.staj.stajyerAdSoyad} · {departmanDegisimi?.staj.donemAd} — şu an{" "}
              {departmanDegisimi?.staj.departmanAd}
            </Typography>
            <Alert severity="warning" sx={{ mb: 2 }}>
              Departman değişince mevcut sorumlular geçersizleşir; asıl ve yedek sorumlu
              yeni departmandan yeniden seçilmek zorunda.
            </Alert>
            <Stack spacing={2}>
              <TextField
                select
                label="Yeni departman"
                value={departmanDegisimi?.departmanHashId ?? ""}
                onChange={(e) =>
                  // Departman degisince sorumlu secimleri gecersiz kalir, sifirla.
                  setDepartmanDegisimi((d) =>
                    d
                      ? {
                          ...d,
                          departmanHashId: e.target.value,
                          asilSorumluHashId: "",
                          yedekSorumluHashId: "",
                        }
                      : d,
                  )
                }
                required
                fullWidth
              >
                {departmanlar.data?.map((d) => (
                  <MenuItem key={d.hashId} value={d.hashId}>
                    {d.ad}
                  </MenuItem>
                ))}
              </TextField>

              {/* Asil zorunlu, yedek opsiyonel: departmanda tek sorumlu calisiyor olabilir. */}
              {(["asil", "yedek"] as const).map((rol) => (
                <TextField
                  key={rol}
                  select
                  label={rol === "asil" ? "Asıl sorumlu" : "Yedek sorumlu"}
                  value={
                    (rol === "asil"
                      ? departmanDegisimi?.asilSorumluHashId
                      : departmanDegisimi?.yedekSorumluHashId) ?? ""
                  }
                  onChange={(e) =>
                    setDepartmanDegisimi((d) =>
                      d
                        ? rol === "asil"
                          ? { ...d, asilSorumluHashId: e.target.value }
                          : { ...d, yedekSorumluHashId: e.target.value }
                        : d,
                    )
                  }
                  required={rol === "asil"}
                  helperText={rol === "yedek" ? "Zorunlu değil; yedek yoksa boş bırakın." : undefined}
                  fullWidth
                  disabled={!departmanDegisimi?.departmanHashId}
                >
                  {rol === "yedek" && <MenuItem value="">(yok)</MenuItem>}
                  {(sorumlular.data ?? [])
                    .filter((s) => s.departmanHashId === departmanDegisimi?.departmanHashId)
                    // Asil ile yedek ayni kisi olamaz; secili asili listeden dusur.
                    .filter(
                      (s) => rol === "asil" || s.hashId !== departmanDegisimi?.asilSorumluHashId,
                    )
                    .map((s) => (
                      <MenuItem key={s.hashId} value={s.hashId}>
                        {s.ad} {s.soyad}
                      </MenuItem>
                    ))}
                </TextField>
              ))}
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDepartmanDegisimi(null)}>Vazgeç</Button>
            <IslemButonu
              type="submit"
              variant="contained"
              calisiyor={departmanDegistir.isPending}
              /* Backend zaten reddeder ama kullanici bosuna denemesin.
                 Yedek bos olabilir (opsiyonel); o zaman esitlik kontrolu anlamsiz. */
              disabled={
                !!departmanDegisimi?.yedekSorumluHashId &&
                departmanDegisimi.asilSorumluHashId === departmanDegisimi.yedekSorumluHashId
              }
            >
              Değiştir
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      {/* ---- Staj duzeltme ---- */}
      <Dialog open={duzenlenen !== null} onClose={() => setDuzenlenen(null)} maxWidth="sm" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (duzenlenen) {
              duzelt.mutate(duzenlenen);
            }
          }}
        >
          <DialogTitle>Stajı Düzenle</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {duzenlenen?.staj.stajyerAdSoyad} — {duzenlenen?.staj.departmanAd} ·{" "}
              {duzenlenen?.staj.donemAd}
            </Typography>
            <Alert severity="info" sx={{ mb: 2 }}>
              Departman ve yedek sorumlu buradan değişmez; onların kendi işlemleri var.
              Devam günlerini değiştirmek geçmiş günlerin "plan dışı" durumunu da etkiler.
            </Alert>
            <Stack spacing={2}>
              <Stack direction="row" spacing={2}>
                <TextField
                  label="Başlangıç"
                  type="date"
                  value={duzenlenen?.stajBaslangicTarihi ?? ""}
                  onChange={(e) =>
                    setDuzenlenen((d) => (d ? { ...d, stajBaslangicTarihi: e.target.value } : d))
                  }
                  InputLabelProps={{ shrink: true }}
                  required
                  fullWidth
                />
                <TextField
                  label="Bitiş"
                  type="date"
                  value={duzenlenen?.stajBitisTarihi ?? ""}
                  onChange={(e) =>
                    setDuzenlenen((d) => (d ? { ...d, stajBitisTarihi: e.target.value } : d))
                  }
                  InputLabelProps={{ shrink: true }}
                  required
                  fullWidth
                />
              </Stack>

              <FormControl fullWidth>
                <InputLabel id="duzenle-devam-gunleri-label">Devam günleri</InputLabel>
                <Select
                  labelId="duzenle-devam-gunleri-label"
                  multiple
                  value={duzenlenen?.devamGunleri ?? []}
                  onChange={(e) =>
                    setDuzenlenen((d) =>
                      d ? { ...d, devamGunleri: e.target.value as HaftaGunu[] } : d,
                    )
                  }
                  input={<OutlinedInput label="Devam günleri" />}
                  renderValue={(secili) =>
                    GUNLER.filter((g) => (secili as HaftaGunu[]).includes(g.deger))
                      .map((g) => g.etiket)
                      .join(", ")
                  }
                >
                  {GUNLER.map((g) => (
                    <MenuItem key={g.deger} value={g.deger}>
                      <Checkbox checked={(duzenlenen?.devamGunleri ?? []).includes(g.deger)} />
                      <ListItemText primary={g.etiket} />
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              {/* ADIM 1 eslesme kurali: yeni asil, stajin departmanina ait olmali. */}
              <TextField
                select
                label="Asıl sorumlu"
                value={duzenlenen?.asilSorumluHashId ?? ""}
                onChange={(e) =>
                  setDuzenlenen((d) => (d ? { ...d, asilSorumluHashId: e.target.value } : d))
                }
                required
                fullWidth
                helperText="Yalnızca stajın departmanındaki sorumlular listelenir."
              >
                {(sorumlular.data ?? [])
                  .filter((s) => s.departmanHashId === duzenlenen?.staj.departmanHashId)
                  .map((s) => (
                    <MenuItem key={s.hashId} value={s.hashId}>
                      {s.ad} {s.soyad}
                    </MenuItem>
                  ))}
              </TextField>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDuzenlenen(null)}>Vazgeç</Button>
            <IslemButonu
              type="submit"
              variant="contained"
              calisiyor={duzelt.isPending}
              disabled={(duzenlenen?.devamGunleri.length ?? 0) === 0}
            >
              Kaydet
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      {/* ---- Yedek sorumlu atama ---- */}
      <Dialog open={yedekAtama !== null} onClose={() => setYedekAtama(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Yedek Sorumlu Ata</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {yedekAtama?.staj.stajyerAdSoyad} — {yedekAtama?.staj.departmanAd} ·{" "}
            {yedekAtama?.staj.donemAd}
          </Typography>
          <TextField
            select
            label="Yedek sorumlu"
            value={yedekAtama?.sorumluHashId ?? ""}
            onChange={(e) =>
              setYedekAtama((y) => (y ? { ...y, sorumluHashId: e.target.value } : y))
            }
            helperText={
              yedekAtama
                ? sorumluIpucu(
                    yedekAtama.staj.departmanHashId ?? null,
                    yedekAdaylari(yedekAtama.staj).length,
                  )
                : undefined
            }
            fullWidth
          >
            {yedekAtama &&
              yedekAdaylari(yedekAtama.staj).map((s) => (
                <MenuItem key={s.hashId} value={s.hashId}>
                  {s.ad} {s.soyad}
                </MenuItem>
              ))}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setYedekAtama(null)}>Vazgeç</Button>
          <IslemButonu
            variant="contained"
            calisiyor={yedekAta.isPending}
            disabled={!yedekAtama?.sorumluHashId}
            onClick={() =>
              yedekAtama &&
              yedekAta.mutate({
                stajHashId: yedekAtama.staj.hashId,
                sorumluHashId: yedekAtama.sorumluHashId,
              })
            }
          >
            Ata
          </IslemButonu>
        </DialogActions>
      </Dialog>

      {/* ---- Puan duzeltme ---- */}
      <Dialog open={puanDuzeltme !== null} onClose={() => setPuanDuzeltme(null)} maxWidth="sm" fullWidth>
        <Box
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            if (puanDuzeltme) {
              puanDuzelt.mutate({
                stajHashId: puanDuzeltme.staj.hashId,
                puan: Number(puanDuzeltme.puan),
                yorum: puanDuzeltme.yorum,
                gerekce: puanDuzeltme.gerekce,
              });
            }
          }}
        >
          <DialogTitle>Puanı Düzelt</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {puanDuzeltme?.staj.stajyerAdSoyad} — {puanDuzeltme?.staj.departmanAd} ·{" "}
              {puanDuzeltme?.staj.donemAd}
            </Typography>
            {mevcutDegerlendirme.isPending && (
              <Alert severity="info" sx={{ mb: 2 }}>
                Mevcut puan alınıyor...
              </Alert>
            )}
            {/* puan null = staj hic puanlanmamis; bu bir hata degil, gecerli ara durum. */}
            {mevcutDegerlendirme.data && mevcutDegerlendirme.data.puan == null && (
              <Alert severity="warning" sx={{ mb: 2 }}>
                Bu staj henüz puanlanmamış. Düzeltme yerine önce sorumlunun puanlaması
                beklenmeli; yine de buradan puan girebilirsiniz.
              </Alert>
            )}
            {mevcutDegerlendirme.data && mevcutDegerlendirme.data.puan != null && (
              <Alert severity="info" sx={{ mb: 2 }}>
                Mevcut puan: <strong>{mevcutDegerlendirme.data.puan}</strong>
                {mevcutDegerlendirme.data.duzeltmeGerekcesi && (
                  <>
                    <br />
                    Önceki düzeltme gerekçesi: {mevcutDegerlendirme.data.duzeltmeGerekcesi}
                  </>
                )}
              </Alert>
            )}
            <Stack spacing={2}>
              <TextField
                label="Yeni puan (0-100)"
                type="number"
                value={puanDuzeltme?.puan ?? ""}
                onChange={(e) => setPuanDuzeltme((p) => (p ? { ...p, puan: e.target.value } : p))}
                inputProps={{ min: 0, max: 100, step: "0.5" }}
                required
                fullWidth
              />
              <TextField
                label="Düzeltme gerekçesi (zorunlu)"
                value={puanDuzeltme?.gerekce ?? ""}
                onChange={(e) => setPuanDuzeltme((p) => (p ? { ...p, gerekce: e.target.value } : p))}
                multiline
                minRows={2}
                required
                fullWidth
                helperText="Kayıtta saklanır; puanın neden değiştirildiği sonradan izlenebilsin diye."
              />
              <TextField
                label="Yorum"
                value={puanDuzeltme?.yorum ?? ""}
                onChange={(e) => setPuanDuzeltme((p) => (p ? { ...p, yorum: e.target.value } : p))}
                multiline
                minRows={2}
                fullWidth
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setPuanDuzeltme(null)}>Vazgeç</Button>
            <IslemButonu
              type="submit"
              variant="contained"
              calisiyor={puanDuzelt.isPending}
              disabled={puanDuzeltme?.puan === "" || puanDuzeltme?.gerekce.trim() === ""}
            >
              Kaydet
            </IslemButonu>
          </DialogActions>
        </Box>
      </Dialog>

      <OnayDialog
        acik={kilitAcilacak !== null}
        baslik="Kilit açılsın mı?"
        metin={`${kilitAcilacak?.stajyerAdSoyad} için kapanmış staj yeniden yazılabilir hale gelir; sorumlu tekrar puanlayabilir.`}
        onayMetni="Kilidi Aç"
        calisiyor={kilidiAc.isPending}
        onOnay={() => kilitAcilacak && kilidiAc.mutate(kilitAcilacak.hashId)}
        onKapat={() => setKilitAcilacak(null)}
      />

      <OnayDialog
        acik={silinecek !== null}
        baslik="Staj silinsin mi?"
        metin={`${silinecek?.stajyerAdSoyad} için açılan staj kaydı silinecek. Bu işlem geri alınamaz.`}
        onayMetni="Sil"
        calisiyor={sil.isPending}
        onOnay={() => silinecek && sil.mutate(silinecek.hashId)}
        onKapat={() => setSilinecek(null)}
      />
    </>
  );
}
