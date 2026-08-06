package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.dto.Auth.ProvisionResponse;
import com.example.StajEnvanterProjesi.entity.dto.Auth.SorumluProvisionRequest;
import com.example.StajEnvanterProjesi.entity.dto.Auth.StajyerProvisionRequest;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.service.ProvisioningService;
import com.example.StajEnvanterProjesi.service.SorumluService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ADIM 2/3 — Admin kullanici uc noktalari (/api/admin/** -> SecurityConfig'te hasRole('ADMIN')).
 * Provisioning (profil + login hesabi) ve sorumlu ayrilma (terfi transaction'i).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final ProvisioningService provisioningService;
    private final SorumluService sorumluService;
    private final SorumluRepository sorumluRepository;
    private final MessageHelper messageHelper;

    public AdminUserController(ProvisioningService provisioningService,
                               SorumluService sorumluService,
                               SorumluRepository sorumluRepository,
                               MessageHelper messageHelper) {
        this.provisioningService = provisioningService;
        this.sorumluService = sorumluService;
        this.sorumluRepository = sorumluRepository;
        this.messageHelper = messageHelper;
    }

    @PostMapping("/sorumlular")
    public ResponseEntity<ProvisionResponse> sorumluOlustur(@Valid @RequestBody SorumluProvisionRequest request) {
        return ResponseEntity.ok(provisioningService.sorumluOlustur(request));
    }

    @PostMapping("/stajyerler")
    public ResponseEntity<ProvisionResponse> stajyerOlustur(@Valid @RequestBody StajyerProvisionRequest request) {
        return ResponseEntity.ok(provisioningService.stajyerOlustur(request));
    }

    /**
     * ADIM 3 — Sorumluyu pasifleştir: terfi mantigi tetiklenir (yedek asile terfi eder,
     * yedek slotlari bosalir, login hesabi kilitlenir). Yedeksiz aktif stajyeri varsa hata.
     */
    @DeleteMapping("/sorumlular/{hashId}")
    public ResponseEntity<Void> sorumluAyril(@PathVariable String hashId) {
        Long sorumluId = sorumluRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.sorumlu.bulunamadi", hashId)))
                .getId();
        sorumluService.sorumluAyril(sorumluId);
        return ResponseEntity.ok().build();
    }
}
