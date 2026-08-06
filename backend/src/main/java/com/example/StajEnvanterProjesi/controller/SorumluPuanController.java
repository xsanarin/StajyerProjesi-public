package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.DegerlendirmeResponse;
import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.PuanRequest;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.PuanlamaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ADIM 4 — Sorumlu puanlama (/api/sorumlu/** -> hasRole('SORUMLU')).
 * Kimlik query param'dan degil token'dan (CurrentUser) gelir; hangi sorumlunun puanladigi
 * boylece guvence altina alinir (istemci baskasinin adina puanlayamaz).
 */
@RestController
@RequestMapping("/api/sorumlu/stajlar")
public class SorumluPuanController {

    private final PuanlamaService puanlamaService;
    private final CurrentUser currentUser;

    public SorumluPuanController(PuanlamaService puanlamaService, CurrentUser currentUser) {
        this.puanlamaService = puanlamaService;
        this.currentUser = currentUser;
    }

    /**
     * FRONTEND ADIMI — Sorumlunun kendi stajinin mevcut degerlendirmesi.
     * Sahiplik kapisi serviste (asil ya da yedek); baskasinin stajinda 403 doner.
     * Puanlanmamis stajda 404 DEGIL, puan'i null olan bir govde doner.
     */
    @GetMapping("/{hashId}/degerlendirme")
    public ResponseEntity<DegerlendirmeResponse> degerlendirmem(@PathVariable String hashId) {
        return ResponseEntity.ok(
                puanlamaService.degerlendirmeGetirSorumlu(hashId, currentUser.sorumluId()));
    }

    @PostMapping("/{hashId}/puanla")
    public ResponseEntity<DegerlendirmeResponse> puanla(@PathVariable String hashId,
                                                        @Valid @RequestBody PuanRequest request) {
        Long sorumluId = currentUser.sorumluId();
        return ResponseEntity.ok(puanlamaService.puanla(hashId, sorumluId, request));
    }
}
