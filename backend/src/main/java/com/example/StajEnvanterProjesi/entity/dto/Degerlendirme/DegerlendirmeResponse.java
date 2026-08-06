package com.example.StajEnvanterProjesi.entity.dto.Degerlendirme;

import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import lombok.Data;

/**
 * ADIM 4 — Istemciye donen degerlendirme ozeti (id yerine hashId - DTO kurali).
 */
@Data
public class DegerlendirmeResponse {
    private String hashId;

    private String stajHashId;
    private String stajyerAdSoyad;

    private Double puan;
    private String yorum;
    private String puanlayanSorumluHashId;
    private String duzeltmeGerekcesi;

    // Puanlama sonrasi stajin son durumu (TAMAMLANDI beklenir).
    private StajDurumu stajDurumu;
}
