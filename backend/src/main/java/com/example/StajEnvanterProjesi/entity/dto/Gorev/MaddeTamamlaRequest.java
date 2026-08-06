package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ADIM 2 — Stajyerin madde isaretlemesi.
 *
 * Madde bir SORU degil, yapilacak IS. Bu yuzden not ZORUNLU DEGIL:
 *   {"tamamlandi": true}                        -> yaptim, not yok
 *   {"tamamlandi": true, "stajyerNotu": "..."}  -> yaptim + aciklama
 *   {"tamamlandi": false}                       -> geri al (BEKLIYOR'a doner)
 *
 * stajyerNotu gonderilmezse (null) mevcut not korunur; bosaltmak icin bos string.
 */
@Data
public class MaddeTamamlaRequest {

    @Size(max = 4000)
    private String stajyerNotu;

    private Boolean tamamlandi;
}
