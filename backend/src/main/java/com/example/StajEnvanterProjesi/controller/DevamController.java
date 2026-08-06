package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamOzetResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.DevamResponse;
import com.example.StajEnvanterProjesi.entity.dto.Devam.ManuelDevamRequest;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.DevamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADIM 1 (KISI-B) — Sorumlu devam takibi (/api/sorumlu/** -> hasRole('SORUMLU')).
 * Kimlik token'dan (CurrentUser) gelir; disariya hashId, iceride Long id (DTO kurali).
 */
@RestController
@RequestMapping("/api/sorumlu/stajlar")
public class DevamController {

    private final DevamService devamService;
    private final StajRepository stajRepository;
    private final CurrentUser currentUser;
    private final MessageHelper messageHelper;

    public DevamController(DevamService devamService, StajRepository stajRepository,
                           CurrentUser currentUser, MessageHelper messageHelper) {
        this.devamService = devamService;
        this.stajRepository = stajRepository;
        this.currentUser = currentUser;
        this.messageHelper = messageHelper;
    }

    // Tek tik giris - govde yok, saat sunucudan.
    @PostMapping("/{hashId}/devam/giris")
    public ResponseEntity<Void> girisYap(@PathVariable String hashId) {
        devamService.girisYap(stajId(hashId), currentUser.sorumluId());
        return ResponseEntity.ok().build();
    }

    // Tek tik cikis.
    @PostMapping("/{hashId}/devam/cikis")
    public ResponseEntity<Void> cikisYap(@PathVariable String hashId) {
        devamService.cikisYap(stajId(hashId), currentUser.sorumluId());
        return ResponseEntity.ok().build();
    }

    // V15 — stajyerin bildirdigi gunu onayla; gun devam yuzdesine sayilmaya baslar.
    @PutMapping("/{stajHashId}/devam/{devamHashId}/onayla")
    public ResponseEntity<DevamResponse> devamOnayla(@PathVariable String stajHashId,
                                                     @PathVariable String devamHashId) {
        return ResponseEntity.ok(
                devamService.devamOnayla(stajId(stajHashId), devamHashId, currentUser.sorumluId()));
    }

    // V15 — bildirilen gunu reddet (kayit silinir). Onaylanmis kayit reddedilemez.
    @DeleteMapping("/{stajHashId}/devam/{devamHashId}")
    public ResponseEntity<Void> devamReddet(@PathVariable String stajHashId,
                                            @PathVariable String devamHashId) {
        devamService.devamReddet(stajId(stajHashId), devamHashId, currentUser.sorumluId());
        return ResponseEntity.noContent().build();
    }

    // Geriye donuk manuel kayit (sorumlu -> sahiplik aranir).
    @PostMapping("/{hashId}/devam/manuel")
    public ResponseEntity<Void> manuelKayit(@PathVariable String hashId,
                                            @Valid @RequestBody ManuelDevamRequest request) {
        devamService.manuelKayit(stajId(hashId), currentUser.sorumluId(), request, false);
        return ResponseEntity.ok().build();
    }

    // Aylik takvim: her gun icin durum (TAM / CIKIS_YOK / GELMEDI).
    @GetMapping("/{hashId}/devam")
    public ResponseEntity<List<DevamResponse>> aylikDevam(@PathVariable String hashId,
                                                          @RequestParam int yil,
                                                          @RequestParam int ay) {
        List<DevamResponse> liste = devamService.aylikDevam(stajId(hashId), currentUser.sorumluId(), yil, ay);
        return ResponseEntity.ok(liste);
    }

    // Aylik ozet: beklenen / gelinen / devamsiz gun sayilari.
    @GetMapping("/{hashId}/devam/ozet")
    public ResponseEntity<DevamOzetResponse> aylikOzet(@PathVariable String hashId,
                                                       @RequestParam int yil,
                                                       @RequestParam int ay) {
        return ResponseEntity.ok(
                devamService.aylikOzet(stajId(hashId), currentUser.sorumluId(), yil, ay));
    }

    // hashId (disari) -> Long id (iceri) cevrimi.
    private Long stajId(String hashId) {
        return stajRepository.findByHashIdAndStatusTrue(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")))
                .getId();
    }
}
