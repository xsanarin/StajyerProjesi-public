package com.example.StajEnvanterProjesi.entity.dto.Sorumlu;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Sorumlu profil guncelleme istegi. Kisitlar SorumluProvisionRequest ile ayni:
 * olustururken zorunlu olan bir alan guncellerken bosaltilabiliyorsa, provisioning'deki
 * dogrulama update uzerinden atlatilabilir hale gelirdi.
 *
 * PUT tam ezme semantigi tasir: gonderilmeyen opsiyonel alan (telefon, departman)
 * temizlenir. Istemci formu her zaman dolu gondermeli.
 */
@Data
public class SorumluRequest {

    @NotBlank
    private String ad;

    @NotBlank
    private String soyad;

    @NotBlank
    @Email
    private String email;

    private String telefon;

    // ADIM 1: sorumlunun departmani (DTO kurali -> disaridan hashId). Opsiyonel:
    // gonderilirse Service ilgili Departman'a baglar, bos ise departmansiz kaydedilir.
    private String departmanHashId;
}
