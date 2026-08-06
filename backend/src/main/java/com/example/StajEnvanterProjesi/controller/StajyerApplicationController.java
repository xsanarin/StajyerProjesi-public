package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.StajyerApplication;
import com.example.StajEnvanterProjesi.service.StajyerApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADIM 0 — DEPRECATED ve KAPATILDI.
 * @RestController anotasyonu kaldirildigi icin bu sinifin hicbir endpoint'i artik
 * Spring tarafindan haritalanmaz (URL'ler 404 doner). Kod referans olsun diye SILINMEDI.
 * Yerine: asil/yedek yonetimi Staj + SorumluServiceImpl.sorumluAyril (ADIM 3).
 */
@Deprecated
// @RestController  // ADIM 0: endpoint'ler kapatildi
@RequestMapping("/stajyerApplication")
public class StajyerApplicationController {

    @Autowired
    private StajyerApplicationService stajyerApplicationService;

    @GetMapping("/getById/{id}")
    public ResponseEntity<StajyerApplication>
    getById(@PathVariable Long id) {

        StajyerApplication response =
                stajyerApplicationService.findById(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<StajyerApplication>>
    getAll() {

        return ResponseEntity.ok(
                stajyerApplicationService.findAll()
        );
    }


    @PostMapping("/save/{stajyerId}/{sorumluId}")
    public ResponseEntity<String> save(
            @PathVariable Long stajyerId,
            @PathVariable Long sorumluId) {

        try {
            StajyerApplication kayit =
                    stajyerApplicationService.stajyerKaydet(stajyerId, sorumluId);

            return ResponseEntity.ok(kayit.toString());

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Yedek sorumlu ekle
    // POST /stajyerApplication/yedekEkle/1/8
    @PostMapping("/yedekEkle/{stajyerId}/{sorumluId}")
    public ResponseEntity<StajyerApplication> yedekEkle(
            @PathVariable Long stajyerId,
            @PathVariable Long sorumluId) {
        return ResponseEntity.ok(
                stajyerApplicationService.yedekSorumluEkle(stajyerId, sorumluId));
    }

    @PutMapping("/sorumluAyril/{stajyerId}")
    public ResponseEntity<StajyerApplication> sorumluAyril(@PathVariable Long stajyerId) {
        StajyerApplication kayit = stajyerApplicationService.sorumluAyril(stajyerId);
        return ResponseEntity.ok(kayit);
    }

    @PostMapping("/sorumluAta/{stajyerId}/{sorumluId}")
    public ResponseEntity<StajyerApplication> sorumluAta(
            @PathVariable Long stajyerId,
            @PathVariable Long sorumluId) {
        StajyerApplication kayit = stajyerApplicationService.yeniSorumluAta(stajyerId, sorumluId);
        return ResponseEntity.ok(kayit);
    }


    // ADIM 3 (KISI-B): /gunKalan KALDIRILDI. Hesap Stajyer.durum + Stajyer.stajBitisTarihi
    // uzerindeydi; ADIM 0'da iki alan da Staj'a tasinince uc bos liste dondurmeye basladi.
    // Yerine: GET /api/sorumlu/yaklasan-bitisler  (sorumlunun kendi stajlari)
    //         GET /api/admin/bildirim/yaklasan-bitisler  (tum departmanlar)

    @PutMapping("/delete/{id}")
    public ResponseEntity<Void>
    delete(@PathVariable Long id) {

        stajyerApplicationService.delete(id);

        return ResponseEntity.ok().build();
    }
}