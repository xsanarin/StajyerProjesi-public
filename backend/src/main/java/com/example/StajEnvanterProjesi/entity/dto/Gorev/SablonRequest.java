package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ADIM 2 — Sablon olusturma/guncelleme (admin).
 * departmanHashId + seviye zorunlu: oneri mekanizmasi ve sablonAta uyum kontrolu
 * bu iki alan uzerinden calisir.
 */
@Data
public class SablonRequest {

    @NotBlank
    @Size(max = 255)
    private String ad;

    @Size(max = 2000)
    private String aciklama;

    @NotBlank
    private String departmanHashId;

    @NotNull
    private Seviye seviye;

    @Valid
    private List<SablonMaddeRequest> maddeler = new ArrayList<>();
}
