package com.example.StajEnvanterProjesi.entity.dto.Staj;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Yanlis departmanla acilmis bir stajin departmanini duzeltir.
 *
 * Departman ve sorumlular TEK ISTEKTE alinir: ADIM 1 kurali geregi asil/yedek
 * sorumlunun departmani stajin departmani ile ayni olmak zorunda, dolayisiyla
 * departman degisince mevcut sorumlular otomatik olarak gecersizlesir. Ayri
 * adimlara bolunse her adim digerinin kuralina takilir (kilitlenme).
 */
@Data
public class DepartmanDegistirRequest {

    @NotBlank
    private String departmanHashId;

    @NotBlank
    private String asilSorumluHashId;

    /** OPSIYONEL: yeni departmanda tek sorumlu varsa staj yedeksiz kalir (null/bos = yedek yok). */
    private String yedekSorumluHashId;
}
