package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import com.example.StajEnvanterProjesi.entity.enums.GorevDurumu;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ADIM 2 — Sorumlunun onay karari. durum yalnizca ONAYLANDI ya da REDDEDILDI olabilir
 * (servis dogrular); BEKLIYOR/TAMAMLANDI onay karari degildir.
 *
 * onayNotu: ONAYLANDI'da opsiyonel, REDDEDILDI'de ZORUNLU (serviste dogrulanir) —
 * stajyer neyi duzeltecegini bilmeli. Not, stajyer maddeyi yeniden tamamlasa bile
 * silinmez; "onceki geri bildirim" olarak durur.
 */
@Data
public class MaddeOnayRequest {

    @NotNull
    private GorevDurumu durum;

    @Size(max = 2000)
    private String onayNotu;
}
