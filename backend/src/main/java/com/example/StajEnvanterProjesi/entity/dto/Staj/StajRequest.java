package com.example.StajEnvanterProjesi.entity.dto.Staj;

import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Admin yeni staj acarken gonderir (plan: POST /api/stajlar).
 * Iliskili varliklari tum obje olarak degil, sadece id ile aliriz -> daha temiz ve guvenli.
 */
@Data
public class StajRequest {
    // DTO kurali: istemci iliskili kayitlari hashId ile secer (Long id disari acilmadigi
    // icin zaten bilemez). Service hashId -> entity cevrimini yapar.
    @NotBlank
    private String stajyerHashId;
    @NotBlank
    private String donemHashId;
    @NotBlank
    private String asilSorumluHashId;
    /** OPSIYONEL: departmanda tek sorumlu calisiyor olabilir (null/bos = yedek yok). */
    private String yedekSorumluHashId;

    // ADIM 1: departman staja baglidir.
    // Service, hashId -> Departman cevrimini yapip stajyer/asil/yedek eslesmesini dogrular.
    @NotBlank
    private String departmanHashId;

    // ADIM 0: Integer 1/2 yerine enum. null gelirse Service varsayilan LEVEL_1 atar.
    private Seviye seviye;

    // ADIM 3 (KISI-B): staj tarihleri ADIM 0'da Stajyer'den Staj'a tasindi ama
    // olusturma yoluna hic baglanmamisti — her staj NULL bitis tarihiyle aciliyordu.
    // Bitisi yaklasan staj bildirimi bu alan olmadan calisamaz.
    private LocalDate stajBaslangicTarihi;

    private LocalDate stajBitisTarihi;
    // V13: haftalik devam plani. Verilmezse tam mesai (Pzt-Cum) varsayilir.
    // java.time.DayOfWeek adlari: ["MONDAY","WEDNESDAY","FRIDAY"]
    private Set<DayOfWeek> devamGunleri;
}
