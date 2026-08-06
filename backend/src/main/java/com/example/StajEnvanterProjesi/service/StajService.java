package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.dto.SayfaResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.DepartmanDegistirRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.DepartmanIstatistikResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajGuncelleRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajResponse;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;

import java.util.List;

public interface StajService {

    /**
     * ORTAK KAPI (KISI-B, ADIM 1 sozlesmesi).
     * Staj'a YAZAN her servis metodu (devam, gorev...) buradan gecer: sahiplik + aktiflik.
     * Kural: cagiran sorumlu stajin ASIL ya da YEDEK'i olmali VE staj DEVAM_EDIYOR olmali.
     * Ihlal: yetkisiz -> 403 (AccessDeniedException), aktif degil -> 400 (BaseException).
     *
     * Not (puanlama ile fark): puanlama SADECE asil ile yapilir; bu metod asil+yedek izin verir.
     * Bu yuzden A'nin PuanlamaServiceImpl'i bu metodu birebir kullanmaz, kendi asil-only
     * kontrolunu korur. Imza degisirse once A'ya haber verilecek (KISI-B.md).
     */
    Staj benimAktifStajim(Long stajId, Long sorumluId);

    StajResponse findById(Long id);

    /**
     * DIKKAT: tum stajlari tek seferde dondurur, sayfalama yoktur.
     * Yeni ekranlarda ara(...) kullanin; bu metod kucuk ic kullanimlar
     * (acilir liste, disa aktarma) icin duruyor.
     */
    List<StajResponse> findAll();

    /**
     * Admin staj listesinin sayfali + aramali okuma yolu.
     * Bos birakilan filtre "hepsi" demektir. departmanHashId bilinmiyorsa
     * BaseException atilir (sessizce tum listeyi donmek yaniltici olurdu).
     */
    SayfaResponse<StajResponse> ara(String q, String departmanHashId, StajDurumu durum,
                                    int sayfa, int boyut);

    /** Istatistik ekrani: departman basina benzersiz stajyer sayisi (SQL'de hesaplanir). */
    List<DepartmanIstatistikResponse> departmanIstatistigi(boolean sadeceAktif);

    // Admin: stajyer + asil + yedek + level ile yeni staj acar.
    StajResponse save(StajRequest request);

    /**
     * Acilmis stajin tarihlerini, devam planini ve asil sorumlusunu duzeltir (admin).
     * Kismi guncelleme: null gelen alan degismez.
     * Sadece DEVAM_EDIYOR stajda calisir; kapanmis staj gecmis veridir.
     */
    StajResponse guncelle(String stajHashId, StajGuncelleRequest request);

    void delete(Long id);

    // Gorev 4: giris yapmis sorumlunun (asil ya da yedek oldugu) stajlari.
    // Simdilik sorumluId'yi disaridan aliyoruz; guvenlik eklenince
    // bu id JWT/SecurityContext'ten gelecek ve parametre kalkacak.
    List<StajResponse> benimStajlarim(Long sorumluId);

    /**
     * ADIM 2 (KISI-B) — /api/me/stajlar: giris yapan stajyerin kendi stajlari.
     * GECMIS DONEMLER DAHIL: tamamlanmis staj salt-okunur gorunur ama listede kalir.
     */
    List<StajResponse> stajyerStajlarim(Long stajyerId);

    /**
     * ADIM 3 — Terfi sonrasi bosalan yedek slotunu doldurur (admin).
     * ADIM 1 departman eslesme kurali burada da uygulanir.
     */
    StajResponse yedekSorumluAta(String stajHashId, String yedekSorumluHashId);

    /**
     * Yanlis departmanla acilmis stajin departmanini duzeltir (admin).
     * Departman + asil + yedek birlikte degisir: ADIM 1 eslesme kurali geregi
     * departman degisince mevcut sorumlular gecersizlesir.
     * Sadece DEVAM_EDIYOR stajda calisir; kapanmis staj gecmis veridir, degismez.
     */
    StajResponse departmanDegistir(String stajHashId, DepartmanDegistirRequest request);

    /**
     * TEK ASIL KURALI dogrulamasi. Sorumlu zaten baska bir DEVAM_EDIYOR stajin asili ise
     * BaseException firlatir.
     *
     * Arayuze acik cunku kurali bozabilecek tek yol staj yazma uclari degil: kapanmis bir
     * staji tekrar acmak (PuanlamaService.adminKilidiAc) da stajin durumunu DEVAM_EDIYOR
     * yapar ve ayni cakismayi yaratir. Kontrol orada da yapilmazsa DB'deki kismi unique
     * index devreye girer ve kullanici anlamli bir hata yerine 500 alir.
     *
     * @param haricStajId hesaba katilmayacak staj (guncellenen/acilan stajin kendisi);
     *                    yeni staj icin null.
     */
    void asilMusaitMiDogrula(Long asilSorumluId, Long haricStajId);
}
