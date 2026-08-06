package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.dto.Departman.DepartmanRequest;
import com.example.StajEnvanterProjesi.entity.dto.Departman.DepartmanResponse;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DepartmanRepository;
import com.example.StajEnvanterProjesi.service.DepartmanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * ADIM 1 — Departman uc noktalari (Donem paterni ile ayni).
 * Ileride (ADIM 2 guvenlik) bu islemler yalnizca ADMIN'e acilacak:
 *   @PreAuthorize("hasRole('ADMIN')")
 */
@RestController
@RequestMapping("/departman")
public class DepartmanController {

    @Autowired
    private DepartmanService departmanService;

    @Autowired
    private DepartmanRepository departmanRepository;

    @Autowired
    private MessageHelper messageHelper;

    @GetMapping("/getById/{hashId}")
    public ResponseEntity<DepartmanResponse> getById(@PathVariable("hashId") String hashId) {
        DepartmanResponse response = departmanService.findById(departmanId(hashId));
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<DepartmanResponse>> getAll() {
        return ResponseEntity.ok(departmanService.findAll());
    }

    @PostMapping("/save")
    public ResponseEntity<DepartmanResponse> save(@Valid @RequestBody DepartmanRequest request) {
        return ResponseEntity.ok(departmanService.save(request));
    }

    @PutMapping("/update/{hashId}")
    public ResponseEntity<DepartmanResponse> update(@PathVariable String hashId,
                                                    @RequestBody DepartmanRequest request) {
        DepartmanResponse response = departmanService.update(departmanId(hashId), request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/delete/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable("hashId") String hashId) {
        departmanService.delete(departmanId(hashId));
        return ResponseEntity.ok().build();
    }

    private Long departmanId(String hashId) {
        return departmanRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")))
                .getId();
    }
}
