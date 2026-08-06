package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import com.example.StajEnvanterProjesi.entity.enums.GorevDurumu;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ADIM 2 — Staja atanmis madde. maddeMetni sablondan alinan KOPYA'dir;
 * sablon sonradan degisse de burasi degismez.
 */
@Data
public class StajGorevMaddeResponse {

    private String hashId;
    private String maddeMetni;
    private Integer sira;
    /** V23: true ise madde sablondan gelmedi, sorumlu bu staja ozel ekledi. */
    private Boolean ozelMi;
    private String stajyerNotu;
    private GorevDurumu durum;
    private LocalDateTime onayTarihi;
    private String onayNotu;
}
