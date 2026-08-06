package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Staj.StajResponse;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.StajService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * FRONTEND ADIMI — Sorumlunun kendi stajyer listesi (/api/sorumlu/** -> hasRole('SORUMLU')).
 *
 * NEDEN YENI BIR UC: liste mantigi (StajService.benimStajlarim) zaten vardi ama disariya
 * yalnizca GET /staj/benim?sorumluHashId=... ile aciliyordu. O yol iki sebeple sorumlu
 * ekranindan kullanilamaz:
 *   1) /staj/** SecurityConfig'te ADMIN'e kilitli — SORUMLU token'i 403 alir.
 *   2) Kimligi query param'dan aliyor; sorumlu kendi hashId'sini bilmez ve bilse bile
 *      baskasinin hashId'sini yazip onun listesini cekebilirdi.
 * Bu uc kimligi token'dan (CurrentUser) okur — istemcinin soyledigine guvenilmez.
 *
 * Ayni kisinin gecmis donemleri ayri Staj satirlari oldugu icin listede ayri satir gelir.
 */
@RestController
@RequestMapping("/api/sorumlu/stajlar")
public class SorumluStajController {

    private final StajService stajService;
    private final CurrentUser currentUser;

    public SorumluStajController(StajService stajService, CurrentUser currentUser) {
        this.stajService = stajService;
        this.currentUser = currentUser;
    }

    /** Giris yapan sorumlunun asil ya da yedek oldugu tum stajlar (gecmis donemler dahil). */
    @GetMapping
    public ResponseEntity<List<StajResponse>> benimStajlarim() {
        return ResponseEntity.ok(stajService.benimStajlarim(currentUser.sorumluId()));
    }
}