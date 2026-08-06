package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Talep.TalepResponse;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.TalepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sorumlunun BT talep ekrani (/api/sorumlu/** -> hasRole('SORUMLU')).
 *
 * YALNIZCA OKUMA: talep olusturma/guncelleme admin yetkisinde. Bu yuzden burada
 * POST/PUT/DELETE ucu bilerek YOK — yetki kontrolu servise birakilmis olmuyor,
 * uc hic acilmiyor.
 *
 * Hangi talepleri gordugu istekten degil TOKEN'dan belirlenir (CurrentUser):
 * disaridan departman parametresi alsaydik sorumlu baska departmanin taleplerini
 * isteyebilirdi.
 */
@RestController
@RequestMapping("/api/sorumlu/talepler")
public class SorumluTalepController {

    private final TalepService talepService;
    private final CurrentUser currentUser;

    public SorumluTalepController(TalepService talepService, CurrentUser currentUser) {
        this.talepService = talepService;
        this.currentUser = currentUser;
    }

    /** Giris yapan sorumlunun departmanina ait talepler. */
    @GetMapping
    public ResponseEntity<List<TalepResponse>> listele() {
        return ResponseEntity.ok(talepService.sorumluListele(currentUser.sorumluId()));
    }
}
