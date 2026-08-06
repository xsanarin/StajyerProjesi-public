package com.example.StajEnvanterProjesi.entity.dto.Stajyer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ADIM 0 sonrasi: staj tarihleri ve durum Stajyer'den Staj'a tasindigi icin
 * bu DTO'dan da cikarildi. Stajyer sadece kisisel bilgi tutar.
 *
 * Kisitlar StajyerProvisionRequest ile ayni: olustururken zorunlu olan bir alan
 * guncellerken bosaltilabiliyorsa, provisioning'deki dogrulama update uzerinden
 * atlatilabilir hale gelirdi.
 *
 * PUT tam ezme semantigi tasir: gonderilmeyen opsiyonel alan (telefon, universite,
 * bolum, sinif) temizlenir. Istemci formu her zaman dolu gondermeli.
 */
@Data
public class StajyerRequest {

        @NotBlank
        private String ad;

        @NotBlank
        private String soyad;

        @NotBlank
        @Email
        private String email;

        private String telefon;

        private String universite;

        private String bolum;

        private Integer sinif;
    }