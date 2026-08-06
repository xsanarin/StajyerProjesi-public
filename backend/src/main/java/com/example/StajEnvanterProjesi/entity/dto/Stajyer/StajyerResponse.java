package com.example.StajEnvanterProjesi.entity.dto.Stajyer;

import lombok.Data;

/**
 * ADIM 0 sonrasi: Stajyer saf kisi bilgisidir; staj tarih/durum alanlari Staj DTO'suna gecti.
 * Disariya id yerine hashId veriyoruz (Long id iceride kalir).
 */
@Data
public class StajyerResponse {

    private String hashId;

    private String ad;

    private String soyad;

    private String email;

    private String telefon;

    private String universite;

    private String bolum;

    private Integer sinif;

    private Boolean status;
}