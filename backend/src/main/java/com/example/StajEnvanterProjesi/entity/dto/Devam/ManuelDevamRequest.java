package com.example.StajEnvanterProjesi.entity.dto.Devam;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Geriye donuk manuel devam kaydi girisi.
 * Kurallar servis katmaninda: gelecek tarih yasak, ayni gune ikinci kayit yasak,
 * cikis varsa giris zorunlu.
 */
@Data
public class ManuelDevamRequest {

    @NotNull
    private LocalDate tarih;

    private LocalDateTime girisSaati;

    private LocalDateTime cikisSaati;
}
