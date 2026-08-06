package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ADIM 2 — Staj seviyesi yukseltme. Yalnizca ILERI yonde; mevcut seviyenin
 * tum gorevleri TAMAMLANDI olmadan yukseltilemez (kurallar serviste).
 */
@Data
public class SeviyeGuncelleRequest {

    @NotNull
    private Seviye seviye;
}
