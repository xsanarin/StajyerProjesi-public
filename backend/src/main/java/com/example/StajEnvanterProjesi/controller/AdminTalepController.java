package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerResponse;
import com.example.StajEnvanterProjesi.entity.dto.Talep.TalepRequest;
import com.example.StajEnvanterProjesi.entity.dto.Talep.TalepResponse;
import com.example.StajEnvanterProjesi.service.TalepService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BT talep yonetimi (/api/admin/** -> hasRole('ADMIN')).
 * Talep olusturma/guncelleme/silme YALNIZCA admin yetkisindedir; sorumlu okur.
 */
@RestController
@RequestMapping("/api/admin/talepler")
public class AdminTalepController {

    private final TalepService talepService;

    public AdminTalepController(TalepService talepService) {
        this.talepService = talepService;
    }

    @PostMapping
    public ResponseEntity<TalepResponse> olustur(@Valid @RequestBody TalepRequest request) {
        return ResponseEntity.ok(talepService.olustur(request));
    }

    @PutMapping("/{hashId}")
    public ResponseEntity<TalepResponse> guncelle(@PathVariable String hashId,
                                                  @Valid @RequestBody TalepRequest request) {
        return ResponseEntity.ok(talepService.guncelle(hashId, request));
    }

    @GetMapping("/{hashId}")
    public ResponseEntity<TalepResponse> getir(@PathVariable String hashId) {
        return ResponseEntity.ok(talepService.getir(hashId));
    }

    @GetMapping
    public ResponseEntity<List<TalepResponse>> listele() {
        return ResponseEntity.ok(talepService.listele());
    }

    /**
     * V22 — Talep dialogundaki kisi seciminin kaynagi: verilen departmanda staj
     * yapan stajyerler.
     *
     * Sabit yol ("/stajyerler") ustteki /{hashId} kalibiyla catismaz: Spring en
     * ozel eslesmeyi secer, sabit segment degiskenden once gelir.
     */
    @GetMapping("/stajyerler")
    public ResponseEntity<List<StajyerResponse>> departmanStajyerleri(
            @RequestParam String departmanHashId) {
        return ResponseEntity.ok(talepService.departmanStajyerleri(departmanHashId));
    }

    /** Soft delete: kayit veritabaninda kalir, listelerden dusar. */
    @DeleteMapping("/{hashId}")
    public ResponseEntity<Void> sil(@PathVariable String hashId) {
        talepService.sil(hashId);
        return ResponseEntity.noContent().build();
    }
}
