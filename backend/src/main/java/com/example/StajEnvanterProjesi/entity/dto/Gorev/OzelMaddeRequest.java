package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * V23 — Sorumlunun goreve sonradan ekledigi, sablonda olmayan madde (ozelMi=true).
 *
 * Size(2000): staj_gorev_madde.madde_metni kolonunun uzunlugu. Dogrulama olmadan
 * uzun metin DB'de kirilir ve kullanici 500 gorurdu.
 */
@Data
public class OzelMaddeRequest {

    @NotBlank
    @Size(max = 2000)
    private String maddeMetni;
}
