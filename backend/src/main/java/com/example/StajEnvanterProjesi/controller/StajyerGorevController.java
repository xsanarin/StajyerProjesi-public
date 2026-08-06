package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Gorev.MaddeTamamlaRequest;
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
 * ADIM 2 (KISI-B) — Stajyer paneli (/api/me/** -> hasRole('STAJYER')).
 *
 * Stajyer YALNIZCA kendi stajlarini gorur ve yalnizca AKTIF stajinda yazabilir;
 * tamamlanmis staj listede kalir ama salt-okunurdur (kontroller serviste).
 */
@RestController
@RequestMapping("/api/me")
public class StajyerGorevController {

    private final StajGorevService gorevService;
    private final CurrentUser currentUser;

    public StajyerGorevController(StajGorevService gorevService, CurrentUser currentUser) {
        this.gorevService = gorevService;
        this.currentUser = currentUser;
    }

    /** Gecmis donemler dahil. */
    @GetMapping("/stajlar")
    public ResponseEntity<List<StajResponse>> stajlarim() {
        return ResponseEntity.ok(gorevService.stajlarim(currentUser.stajyerId()));
    }

    @GetMapping("/gorevler")
    public ResponseEntity<List<StajGorevResponse>> gorevlerim(@RequestParam String stajHashId) {
        return ResponseEntity.ok(gorevService.gorevlerim(stajHashId, currentUser.stajyerId()));
    }

    /** Cevap yaz / maddeyu tamamla. */
    @PatchMapping("/maddeler/{hashId}")
    public ResponseEntity<StajGorevMaddeResponse> maddeTamamla(@PathVariable String hashId,
                                                             @Valid @RequestBody MaddeTamamlaRequest request) {
        return ResponseEntity.ok(gorevService.maddeTamamla(hashId, request, currentUser.stajyerId()));
    }
}
