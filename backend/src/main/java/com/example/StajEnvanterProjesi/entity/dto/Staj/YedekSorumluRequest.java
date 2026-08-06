package com.example.StajEnvanterProjesi.entity.dto.Staj;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ADIM 3 — Bosalan yedek slotunu doldururken atanacak sorumlu.
 * DTO kurali geregi sorumlu hashId ile alinir.
 */
@Data
public class YedekSorumluRequest {

    @NotNull
    private String sorumluHashId;
}
