package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.dto.Donem.DonemRequest;
import com.example.StajEnvanterProjesi.entity.dto.Donem.DonemResponse;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DonemRepository;
import com.example.StajEnvanterProjesi.service.DonemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Donem uc noktalari. Ileride (Faz 1 guvenlik adimi) bu islemler yalnizca ADMIN'e acilacak:
 *   @PreAuthorize("hasRole('ADMIN')")
 */
@RestController
@RequestMapping("/donem")
public class DonemController {

    @Autowired
    private DonemService donemService;

    @Autowired
    private DonemRepository donemRepository;

    @Autowired
    private MessageHelper messageHelper;

    @GetMapping("/getById/{hashId}")
    public ResponseEntity<DonemResponse> getById(@PathVariable("hashId") String hashId) {
        DonemResponse response = donemService.findById(donemId(hashId));
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<DonemResponse>> getAll() {
        return ResponseEntity.ok(donemService.findAll());
    }

    @PostMapping("/save")
    public ResponseEntity<DonemResponse> save(@Valid @RequestBody DonemRequest request) {
        return ResponseEntity.ok(donemService.save(request));
    }

    @PutMapping("/update/{hashId}")
    public ResponseEntity<DonemResponse> update(@PathVariable String hashId,
                                                @RequestBody DonemRequest request) {
        DonemResponse response = donemService.update(donemId(hashId), request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // Bir donemi aktif yapar (digerlerini pasife ceker).
    @PutMapping("/aktifYap/{hashId}")
    public ResponseEntity<DonemResponse> aktifYap(@PathVariable String hashId) {
        DonemResponse response = donemService.aktifYap(donemId(hashId));
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/delete/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable("hashId") String hashId) {
        donemService.delete(donemId(hashId));
        return ResponseEntity.ok().build();
    }

    private Long donemId(String hashId) {
        return donemRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.donem.bulunamadi")))
                .getId();
    }
}
