package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.Degerlendirme;
import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.dto.Bildirim.YaklasanBitisResponse;

import java.util.List;

/**
 * ADIM 3 (KISI-B) — Bitisi yaklasan stajlarin bildirimi.
 *
 * Eski `gunKalanHesapla` Stajyer.durum (String) ve Stajyer.stajBitisTarihi'ne
 * dayaniyordu; ADIM 0'da iki alan da Staj'a tasindi. Hesap bu servise tasindi
 * ve StajyerApplication'a olan bagimlilik koptu.
 */
public interface BildirimService {

    /**
     * Tum bolumler — admin gorunumu.
     *
     * @param q stajyer / departman / sorumlu adinda gecen metin; bos ya da null ise suzme yok
     */
    List<YaklasanBitisResponse> yaklasanBitisler(int gun, String q);

    /** Yalnizca giris yapan sorumlunun (asil ya da yedek oldugu) stajlari. */
    List<YaklasanBitisResponse> benimYaklasanBitislerim(Long sorumluId, int gun, String q);

    /**
     * Zamanlanmis is: her is gunu 08:00'de bitisi yaklasan stajlari sorumlu
     * bazinda gruplayip mail atar. Elle de tetiklenebilsin diye public.
     *
     * @return bildirim gonderilen sorumlu sayisi
     */
    int bitisiYaklasanlariBildir();

    /**
     * Puanlama tamamlanip staj kapaninca stajyere gonderilen kapanis maili:
     * puan, sorumlu yorumu, staj tarihleri ve gorev ilerleme ozeti.
     *
     * Gonderim puanlamayi BLOKLAMAZ: adres yoksa ya da SMTP kapaliysa sessizce
     * log'a duser. Lazy koleksiyonlar (gorev maddeleri) yuklendigi icin cagiran
     * transaction hala acikken cagrilmalidir.
     *
     * @return mail gercekten gonderildiyse true; aksi halde false
     */
    boolean degerlendirmeTamamlandiBildir(Staj staj, Degerlendirme degerlendirme);
}
