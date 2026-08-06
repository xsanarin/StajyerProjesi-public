package com.example.StajEnvanterProjesi.entity.dto.Staj;

import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Istemciye donen staj ozeti. Ic ice tum entity'leri donmek yerine
 * ekranda lazim olan alanlari duzlestirip veriyoruz (isim + id).
 */
@Data
public class StajResponse {
    // ADIM 0 DTO kurali: disariya SADECE hashId. Long id ve iliskili kayitlarin
    // Long id'leri disari sizmaz (sirali id tahmin edilebilir -> enumeration riski).
    private String hashId;

    private String stajyerHashId;
    private String stajyerAdSoyad;

    private String donemHashId;
    private String donemAd;

    // ADIM 1: departman staja bagli -> response'a da eklendi.
    private String departmanHashId;
    private String departmanAd;

    private String asilSorumluHashId;
    private String asilSorumluAdSoyad;

    private String yedekSorumluHashId;
    private String yedekSorumluAdSoyad;

    // ADIM 0: String/Integer yerine tip guvenli enum'lar.
    private StajDurumu durum;
    private Seviye seviye;

    // ADIM 3: bitis tarihi ekranlarda ve bildirim kutusunda lazim.
    private LocalDate stajBaslangicTarihi;
    private LocalDate stajBitisTarihi;
    // V13: stajyer haftanin hangi gunleri geliyor?
    private Set<DayOfWeek> devamGunleri;
}
