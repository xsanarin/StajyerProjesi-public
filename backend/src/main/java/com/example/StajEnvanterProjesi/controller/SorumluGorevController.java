package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Gorev.IlerlemeResponse;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.OzelMaddeRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonAtaRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonResponse;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SeviyeGuncelleRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.MaddeOnayRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.StajGorevResponse;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.StajGorevMaddeResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajResponse;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.StajGorevService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADIM 2 (KISI-B) — Sorumlunun gorev ekrani (/api/sorumlu/** -> hasRole('SORUMLU')).
 * Kimlik token'dan (CurrentUser); disariya hashId, iceride Long id.
 */
@RestController
@RequestMapping("/api/sorumlu")
public class SorumluGorevController {

    private final StajGorevService gorevService;
    private final CurrentUser currentUser;

    public SorumluGorevController(StajGorevService gorevService, CurrentUser currentUser) {
        this.gorevService = gorevService;
        this.currentUser = currentUser;
    }

    // ---- Sablon onerisi + atama ----

    @GetMapping("/stajlar/{hashId}/onerilen-sablonlar")
    public ResponseEntity<List<SablonResponse>> onerilenSablonlar(@PathVariable String hashId) {
        return ResponseEntity.ok(gorevService.onerilenSablonlar(hashId, currentUser.sorumluId()));
    }

    @PostMapping("/stajlar/{hashId}/gorevler")
    public ResponseEntity<StajGorevResponse> sablonAta(@PathVariable String hashId,
                                                       @Valid @RequestBody SablonAtaRequest request) {
        return ResponseEntity.ok(
                gorevService.sablonAta(hashId, request.getSablonHashId(), currentUser.sorumluId()));
    }

    @GetMapping("/stajlar/{hashId}/gorevler")
    public ResponseEntity<List<StajGorevResponse>> stajGorevleri(@PathVariable String hashId) {
        return ResponseEntity.ok(gorevService.stajGorevleri(hashId, currentUser.sorumluId()));
    }

    // ---- Seviye ----

    @PatchMapping("/stajlar/{hashId}/seviye")
    public ResponseEntity<StajResponse> seviyeYukselt(@PathVariable String hashId,
                                                      @Valid @RequestBody SeviyeGuncelleRequest request) {
        return ResponseEntity.ok(
                gorevService.seviyeYukselt(hashId, request.getSeviye(), currentUser.sorumluId()));
    }

    // ---- Gorev ici islemler ----

    @GetMapping("/gorevler/{hashId}/ilerleme")
    public ResponseEntity<IlerlemeResponse> ilerleme(@PathVariable String hashId) {
        return ResponseEntity.ok(gorevService.ilerleme(hashId, currentUser.sorumluId()));
    }

    /** V23 — Sablonda olmayan madde bu staja ozel eklenir; sablon degismez. */
    @PostMapping("/gorevler/{hashId}/madde")
    public ResponseEntity<StajGorevResponse> ozelMaddeEkle(@PathVariable String hashId,
                                                           @Valid @RequestBody OzelMaddeRequest request) {
        return ResponseEntity.ok(gorevService.ozelMaddeEkle(hashId, request, currentUser.sorumluId()));
    }

    /** V23 — Yalnizca ozel (sablondan gelmeyen) ve onaylanmamis madde silinebilir. */
    @DeleteMapping("/maddeler/{hashId}")
    public ResponseEntity<StajGorevResponse> ozelMaddeSil(@PathVariable String hashId) {
        return ResponseEntity.ok(gorevService.ozelMaddeSil(hashId, currentUser.sorumluId()));
    }

    @PatchMapping("/maddeler/{hashId}/onay")
    public ResponseEntity<StajGorevMaddeResponse> maddeOnayla(@PathVariable String hashId,
                                                            @Valid @RequestBody MaddeOnayRequest request) {
        return ResponseEntity.ok(gorevService.maddeOnayla(hashId, request, currentUser.sorumluId()));
    }
}
