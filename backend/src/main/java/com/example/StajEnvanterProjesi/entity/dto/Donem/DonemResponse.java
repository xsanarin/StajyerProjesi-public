package com.example.StajEnvanterProjesi.entity.dto.Donem;

import lombok.Data;

import java.time.LocalDate;

/**
 * Disariya (istemciye) dondugumuz donem verisi.
 * Entity'yi dogrudan donmuyoruz -> ic alanlari (status, createDate vs.) sizdirmayalim.
 */
@Data
public class DonemResponse {
    // DTO kurali (ADIM 0): disariya Long id degil hashId.
    private String hashId;
    private String ad;
    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;
    private Boolean aktif;
}
