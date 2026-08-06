package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Bildirim.YaklasanBitisResponse;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.BildirimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ADIM 3 (KISI-B) — Sorumlunun "yaklasan bitisler" kutusu.
 *
 * KISI-B.md: "endpoint'i sen ver, ekrani A yapar" — A'nin sorumlu sayfasindaki
 * kutu bu ucu cagirir. Yalnizca giris yapan sorumlunun (asil ya da yedek oldugu)
 * stajlari doner; baskasinin stajyeri gorunmez.
 */
@RestController
@RequestMapping("/api/sorumlu")
public class BildirimController {

    private final BildirimService bildirimService;
    private final CurrentUser currentUser;

    public BildirimController(BildirimService bildirimService, CurrentUser currentUser) {
        this.bildirimService = bildirimService;
        this.currentUser = currentUser;
    }

    /** q: stajyer, departman ya da sorumlu adinda arar; verilmezse suzme yok. */
    @GetMapping("/yaklasan-bitisler")
    public ResponseEntity<List<YaklasanBitisResponse>> benimYaklasanBitislerim(
            @RequestParam(defaultValue = "5") int gun,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(
                bildirimService.benimYaklasanBitislerim(currentUser.sorumluId(), gun, q));
    }
}
