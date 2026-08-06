package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonRequest;
import com.example.StajEnvanterProjesi.entity.dto.Gorev.SablonResponse;
import com.example.StajEnvanterProjesi.service.GorevSablonuService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADIM 2 (KISI-B) — Sablon yonetimi (/api/admin/** -> hasRole('ADMIN')).
 *
 * Sablon SILINMEZ, pasiflestirilir: gecmis atamalarin izi korunsun diye.
 */
@RestController
@RequestMapping("/api/admin/sablonlar")
public class AdminSablonController {

    private final GorevSablonuService sablonService;

    public AdminSablonController(GorevSablonuService sablonService) {
        this.sablonService = sablonService;
    }

    @PostMapping
    public ResponseEntity<SablonResponse> olustur(@Valid @RequestBody SablonRequest request) {
        return ResponseEntity.ok(sablonService.olustur(request));
    }

    @PutMapping("/{hashId}")
    public ResponseEntity<SablonResponse> guncelle(@PathVariable String hashId,
                                                   @Valid @RequestBody SablonRequest request) {
        return ResponseEntity.ok(sablonService.guncelle(hashId, request));
    }

    @GetMapping("/{hashId}")
    public ResponseEntity<SablonResponse> getir(@PathVariable String hashId) {
        return ResponseEntity.ok(sablonService.getir(hashId));
    }

    @GetMapping
    public ResponseEntity<List<SablonResponse>> listele(
            @RequestParam(defaultValue = "false") boolean sadeceAktif,
            @RequestParam(required = false) String departmanHashId) {
        return ResponseEntity.ok(sablonService.listele(sadeceAktif, departmanHashId));
    }

    /** Pasiflestirme (aktif=false) — kalici silme degil. */
    @DeleteMapping("/{hashId}")
    public ResponseEntity<Void> pasiflestir(@PathVariable String hashId) {
        sablonService.pasiflestir(hashId);
        return ResponseEntity.noContent().build();
    }
}
