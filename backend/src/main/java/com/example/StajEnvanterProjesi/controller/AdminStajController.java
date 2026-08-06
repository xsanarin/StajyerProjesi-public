package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.DegerlendirmeResponse;
import com.example.StajEnvanterProjesi.entity.dto.Degerlendirme.PuanDuzeltRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.DepartmanDegistirRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.YedekSorumluRequest;
import com.example.StajEnvanterProjesi.service.PuanlamaService;
import com.example.StajEnvanterProjesi.service.StajService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ADIM 3/4 — Admin staj yonetimi (/api/admin/** -> hasRole('ADMIN')).
 * Terfi sonrasi yedek doldurma (ADIM 3) + puan duzeltme / kilit acma (ADIM 4).
 */
@RestController
@RequestMapping("/api/admin/stajlar")
public class AdminStajController {

    private final StajService stajService;
    private final PuanlamaService puanlamaService;

    public AdminStajController(StajService stajService, PuanlamaService puanlamaService) {
        this.stajService = stajService;
        this.puanlamaService = puanlamaService;
    }

    @PutMapping("/{hashId}/yedek-sorumlu")
    public ResponseEntity<StajResponse> yedekSorumluAta(@PathVariable String hashId,
                                                        @Valid @RequestBody YedekSorumluRequest request) {
        return ResponseEntity.ok(stajService.yedekSorumluAta(hashId, request.getSorumluHashId()));
    }

    /**
     * Yanlis departmanla acilmis stajin departmanini duzeltir.
     * Departman + asil + yedek birlikte gonderilir (bkz. DepartmanDegistirRequest).
     */
    @PutMapping("/{hashId}/departman")
    public ResponseEntity<StajResponse> departmanDegistir(@PathVariable String hashId,
                                                          @Valid @RequestBody DepartmanDegistirRequest request) {
        return ResponseEntity.ok(stajService.departmanDegistir(hashId, request));
    }

    /**
     * FRONTEND ADIMI — Stajin mevcut degerlendirmesi (puan + yorum + duzeltme gerekcesi).
     * Puanlanmamis stajda 404 DEGIL, puan'i null olan bir govde doner; duzeltme ekrani
     * "henuz puanlanmamis" ile "staj yok" durumlarini boylece ayirir.
     */
    @GetMapping("/{hashId}/degerlendirme")
    public ResponseEntity<DegerlendirmeResponse> degerlendirmeGetir(@PathVariable String hashId) {
        return ResponseEntity.ok(puanlamaService.degerlendirmeGetir(hashId));
    }

    // ADIM 4 - Admin puan duzeltme (gerekce zorunlu).
    @PutMapping("/{hashId}/puan")
    public ResponseEntity<DegerlendirmeResponse> puanDuzelt(@PathVariable String hashId,
                                                            @Valid @RequestBody PuanDuzeltRequest request) {
        return ResponseEntity.ok(puanlamaService.adminPuanDuzelt(hashId, request));
    }

    // ADIM 4 - Uc durum: kapanmis stajin yazma kilidini ac.
    @PutMapping("/{hashId}/kilidi-ac")
    public ResponseEntity<DegerlendirmeResponse> kilidiAc(@PathVariable String hashId) {
        return ResponseEntity.ok(puanlamaService.adminKilidiAc(hashId));
    }
}
