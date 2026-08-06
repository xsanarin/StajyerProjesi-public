package com.example.StajEnvanterProjesi.entity.dto.Sorumlu;

import lombok.Data;

@Data
public class SorumluResponse {
    // DTO kurali: admin staj formu asil/yedek sorumluyu hashId ile secer.
    private String hashId;

    private String ad;
    private String soyad;
    private String email;
    private String telefon;

    // ADIM 1: sorumlunun bagli oldugu departman (varsa).
    private String departmanHashId;
    private String departmanAd;
}
