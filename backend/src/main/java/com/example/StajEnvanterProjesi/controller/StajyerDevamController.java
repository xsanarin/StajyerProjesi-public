package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamOzetResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamResponse;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.DevamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * FRONTEND ADIMI — Stajyerin kendi devam takvimi (/api/me/** -> hasRole('STAJYER')).
 *
 * /me ekraninda "devam takvimim" isteniyordu ama devam uclarinin tamami
 * /api/sorumlu/** altindaydi, yani stajyer token'i 403 aliyordu. Bu uc ayni
 * takvim ciktisini stajyerin kendi stajlari icin acar.
 *
 * V15: stajyer artik kendi giris/cikisini BILDIREBILIR ama onaylayamaz — kayit
 * "onay bekliyor" olarak acilir, sorumlu onaylayana kadar devama sayilmaz.
 * Geriye donuk manuel kayit hala sorumlunun/adminin isi.
 */
@RestController
@RequestMapping("/api/me/stajlar")
public class StajyerDevamController {

    private final DevamService devamService;
    private final CurrentUser currentUser;

    public StajyerDevamController(DevamService devamService, CurrentUser currentUser) {
        this.devamService = devamService;
        this.currentUser = currentUser;
    }

    /**
     * V15 — stajyer kendi gelisini bildirir. Kayit ONAY BEKLIYOR durumunda acilir;
     * sorumlu onaylayana kadar devam yuzdesine sayilmaz.
     */
    @PostMapping("/{hashId}/devam/giris")
    public ResponseEntity<Void> girisBildir(@PathVariable String hashId) {
        devamService.stajyerGirisYap(hashId, currentUser.stajyerId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{hashId}/devam/cikis")
    public ResponseEntity<Void> cikisBildir(@PathVariable String hashId) {
        devamService.stajyerCikisYap(hashId, currentUser.stajyerId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{hashId}/devam")
    public ResponseEntity<List<DevamResponse>> takvimim(@PathVariable String hashId,
                                                        @RequestParam int yil,
                                                        @RequestParam int ay) {
        return ResponseEntity.ok(
                devamService.stajyerAylikDevam(hashId, currentUser.stajyerId(), yil, ay));
    }

    @GetMapping("/{hashId}/devam/ozet")
    public ResponseEntity<DevamOzetResponse> ozetim(@PathVariable String hashId,
                                                     @RequestParam int yil,
                                                     @RequestParam int ay) {
        return ResponseEntity.ok(
                devamService.stajyerAylikOzet(hashId, currentUser.stajyerId(), yil, ay));
    }
}