package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.StajyerRepository;
import com.example.StajEnvanterProjesi.service.StajyerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.StajEnvanterProjesi.entity.dto.SayfaResponse;
import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerResponse;
import com.example.StajEnvanterProjesi.entity.dto.Stajyer.StajyerRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/stajyer")
public class StajyerController {

    @Autowired
    StajyerService stajyerService;

    @Autowired
    StajyerRepository stajyerRepository;

    @Autowired
    MessageHelper messageHelper;

    @GetMapping("/getById/{hashId}")
    public ResponseEntity<StajyerResponse> getById(@PathVariable("hashId") String hashId) {
        StajyerResponse response = stajyerService.findById(stajyerId(hashId));
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * DIKKAT: sayfalama yok, tum stajyerleri doner. Liste ekranlari /stajyer/ara
     * kullanmali; bu uc staj acma dialogundaki acilir liste icin duruyor.
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<StajyerResponse>> getAll() {
        return ResponseEntity.ok(stajyerService.findAll());
    }

    /**
     * Sayfali + aramali stajyer listesi.
     * Ornek: /stajyer/ara?q=ahmet&sayfa=0&boyut=25 — boyut en fazla 100.
     */
    @GetMapping("/ara")
    public ResponseEntity<SayfaResponse<StajyerResponse>> ara(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int sayfa,
            @RequestParam(defaultValue = "25") int boyut) {
        return ResponseEntity.ok(stajyerService.ara(q, sayfa, boyut));
    }

    @PostMapping("/save")
    public ResponseEntity<StajyerResponse> save(@Valid @RequestBody StajyerRequest request) {
        return ResponseEntity.ok(stajyerService.save(request));
    }

    @PutMapping("/update/{hashId}")
    public ResponseEntity<StajyerResponse> update(@PathVariable String hashId,
                                                  @Valid @RequestBody StajyerRequest request) {
        StajyerResponse response = stajyerService.update(stajyerId(hashId), request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/delete/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable("hashId") String hashId) {
        stajyerService.delete(stajyerId(hashId));
        return ResponseEntity.ok().build();
    }

    private Long stajyerId(String hashId) {
        return stajyerRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.stajyer.bulunamadi", hashId)))
                .getId();
    }
}


























//@RestController
//@RequestMapping("/stajyer")
//public class StajyerController {
//
//    @Autowired
//    StajyerService stajyerService;
//
//    @GetMapping("/getById/{id}")
//    public ResponseEntity<Optional<StajyerResponse>> getById(@PathVariable ("id") Long id) {
//        Optional<StajyerResponse> stajyer = stajyerService.findById(id);
//
//        return ResponseEntity.ok(stajyer);
//    }
//
//    @GetMapping("/getAll")
//    public List<StajyerResponse> getAll() {
//        return stajyerService.findAll();
//    }
//
//    @PostMapping("/save")
//    public ResponseEntity<Stajyer> save(@RequestBody Stajyer stajyer) {
//        stajyerService.save(stajyer);
//        return ResponseEntity.ok(stajyer);
//    }
//
//
//
//    @PutMapping("/update/{id}")
//    public ResponseEntity<Stajyer> update(@PathVariable Long id, @RequestBody Stajyer stajyer) {
//        Stajyer updatedStajyer = stajyerService.update(id, stajyer);
//
//        if (updatedStajyer == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(updatedStajyer);
//    }
//
//    @PutMapping("/delete/{id}")
//    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
//        stajyerService.delete(id);
//        return ResponseEntity.ok().build();
//    }
//
//
//}
