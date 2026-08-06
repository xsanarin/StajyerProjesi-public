package com.example.StajEnvanterProjesi.service;





import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamGuncelleRequest;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamOzetResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.ManuelDevamRequest;

import java.util.List;

public interface DevamService {

    // Tek tik giris/cikis - saat sunucudan alinir.
    void girisYap(Long stajId, Long sorumluId);

    void cikisYap(Long stajId, Long sorumluId);

    // Geriye donuk manuel kayit. adminMi=true ise sahiplik aranmaz.
    void manuelKayit(Long stajId, Long sorumluId, ManuelDevamRequest req, boolean adminMi);

    /**
     * V15 — CIFT BEYAN. Stajyer kendi gelisini bildirir; kayit onaylandi=false acilir ve
     * sorumlu onaylayana kadar devam yuzdesine SAYILMAZ. Tek beyanli sistemde "geldi"
     * bilgisi yalnizca sorumlunun tiklamasina bagliydi ve hicbir sey dogrulamiyordu.
     */
    void stajyerGirisYap(String stajHashId, Long stajyerId);

    /**
     * Cikis bildirimi gunu YENIDEN onaya dusurur: onay girisi degil gunun tamamini
     * kapsar, dolayisiyla sorumlu erken onayladiysa cikis saatini de gormeli.
     */
    void stajyerCikisYap(String stajHashId, Long stajyerId);

    /** V15 — Sorumlu, stajyerin bildirdigi gunu onaylar; gun devama sayilmaya baslar. */
    DevamResponse devamOnayla(Long stajId, String devamHashId, Long sorumluId);

    /** V15 — Sorumlu, bildirilen gunu reddeder (kayit silinir). Onayli kayit reddedilemez. */
    void devamReddet(Long stajId, String devamHashId, Long sorumluId);

    // Aylik takvim: her gun icin durum (TAM / CIKIS_YOK / GELMEDI).
    List<DevamResponse> aylikDevam(Long stajId, Long sorumluId, int yil, int ay);

    /**
     * Admin: herhangi bir stajin aylik takvimi. Sorumlu ucundan tek farki sahiplik
     * ARANMAMASI — admin zaten tum stajlari yonetir. Duzeltme/silme uclari vardi ama
     * okuma ucu yoktu; admin duzeltecegi kaydi goremiyordu.
     */
    List<DevamResponse> adminAylikDevam(Long stajId, int yil, int ay);

    /** Admin: herhangi bir stajin aylik ozeti (sahiplik aranmaz). */
    DevamOzetResponse adminAylikOzet(Long stajId, int yil, int ay);

    // Admin: belirli bir devam kaydini sil (yanlis/mukerrer kayit temizligi).
    void devamSil(Long stajId, String devamHashId);

    // Admin: belirli bir devam kaydinin giris/cikis saatini duzelt.
    DevamResponse devamGuncelle(Long stajId, String devamHashId, DevamGuncelleRequest req);

    /**
     * Aylik ozet: kac gun beklenirdi, kac gun gelindi, kac gun devamsiz.
     * Haftalik devam plani (Staj.devamGunleri) olmadan bu hesap yapilamiyordu.
     */
    DevamOzetResponse aylikOzet(Long stajId, Long sorumluId, int yil, int ay);

    /**
     * FRONTEND ADIMI — /me ekrani icin stajyerin KENDI takvimi.
     * Sorumlu ucu ile ayni cikti; tek fark sahipligin stajyere gore aranmasi.
     * Long id degil hashId alir: stajyer tarafinda hashId -> id cevrimini yapacak
     * bir admin yolu yok, cevrim sahiplik kontrolu ile ayni yerde yapilir.
     */
    List<DevamResponse> stajyerAylikDevam(String stajHashId, Long stajyerId, int yil, int ay);

    /** FRONTEND ADIMI — /me ekrani icin stajyerin kendi aylik ozeti. */
    DevamOzetResponse stajyerAylikOzet(String stajHashId, Long stajyerId, int yil, int ay);
}
