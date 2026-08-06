package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamGuncelleRequest;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamOzetResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.ManuelDevamRequest;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.service.DevamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADIM 1 (KISI-B) — Admin devam mudahalesi (/api/admin/** -> hasRole('ADMIN')).
 * Admin manuel kayitta sahiplik ARANMAZ (adminMi=true); diger kurallar (gelecek tarih,
 * mukerrer gun, girissiz cikis) aynen gecerli.
 */
@RestController
@RequestMapping("/api/admin/stajlar")
public class AdminDevamController {

    private final DevamService devamService;
    private final StajRepository stajRepository;
    private final MessageHelper messageHelper;

    public AdminDevamController(DevamService devamService, StajRepository stajRepository,
                                MessageHelper messageHelper) {
        this.devamService = devamService;
        this.stajRepository = stajRepository;
        this.messageHelper = messageHelper;
    }

    // Duzeltilecek kaydi gorebilmek icin okuma ucu (sahiplik aranmaz, rol yeter).
    @GetMapping("/{hashId}/devam")
    public ResponseEntity<List<DevamResponse>> aylikDevam(@PathVariable String hashId,
                                                          @RequestParam int yil,
                                                          @RequestParam int ay) {
        return ResponseEntity.ok(devamService.adminAylikDevam(stajId(hashId), yil, ay));
    }

    @GetMapping("/{hashId}/devam/ozet")
    public ResponseEntity<DevamOzetResponse> aylikOzet(@PathVariable String hashId,
                                                       @RequestParam int yil,
                                                       @RequestParam int ay) {
        return ResponseEntity.ok(devamService.adminAylikOzet(stajId(hashId), yil, ay));
    }

    @PostMapping("/{hashId}/devam/manuel")
    public ResponseEntity<Void> manuelKayit(@PathVariable String hashId,
                                            @Valid @RequestBody ManuelDevamRequest request) {
        // adminMi=true -> sahiplik aranmaz, sorumluId gerekmez.
        devamService.manuelKayit(stajId(hashId), null, request, true);
        return ResponseEntity.ok().build();
    }

    // Belirli bir devam kaydinin giris/cikis saatini duzelt.
    @PutMapping("/{stajHashId}/devam/{devamHashId}")
    public ResponseEntity<DevamResponse> devamGuncelle(@PathVariable String stajHashId,
                                                       @PathVariable String devamHashId,
                                                       @Valid @RequestBody DevamGuncelleRequest request) {
        DevamResponse guncel = devamService.devamGuncelle(stajId(stajHashId), devamHashId, request);
        return ResponseEntity.ok(guncel);
    }

    // Yanlis/mukerrer bir devam kaydini sil.
    @DeleteMapping("/{stajHashId}/devam/{devamHashId}")
    public ResponseEntity<Void> devamSil(@PathVariable String stajHashId,
                                         @PathVariable String devamHashId) {
        devamService.devamSil(stajId(stajHashId), devamHashId);
        return ResponseEntity.noContent().build();
    }

    private Long stajId(String hashId) {
        return stajRepository.findByHashIdAndStatusTrue(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")))
                .getId();
    }
}
