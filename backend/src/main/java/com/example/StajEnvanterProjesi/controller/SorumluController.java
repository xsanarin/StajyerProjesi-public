package com.example.StajEnvanterProjesi.controller;
import org.springframework.web.bind.annotation.RestController;
import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.dto.Sorumlu.SorumluRequest;
import com.example.StajEnvanterProjesi.entity.dto.Sorumlu.SorumluResponse;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.service.SorumluService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * DTO kurali (ADIM 0): uc noktalar hashId alir, Long id disari acilmaz.
 */
@RestController
@RequestMapping("/sorumlu")
public class SorumluController {

    @Autowired
    private SorumluService sorumluService;

    @Autowired
    private SorumluRepository sorumluRepository;

    @Autowired
    private MessageHelper messageHelper;

    @GetMapping("/getById/{hashId}")
    public ResponseEntity<SorumluResponse> getById(@PathVariable("hashId") String hashId) {
        SorumluResponse response = sorumluService.findById(sorumluId(hashId));
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<SorumluResponse>> getAll() {
        return ResponseEntity.ok(sorumluService.findAll());
    }

    @PutMapping("/update/{hashId}")
    public ResponseEntity<SorumluResponse> update(@PathVariable String hashId,
                                                  @Valid @RequestBody SorumluRequest request) {
        SorumluResponse response = sorumluService.update(sorumluId(hashId), request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/delete/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable("hashId") String hashId) {
        sorumluService.delete(sorumluId(hashId));
        return ResponseEntity.ok().build();
    }

    private Long sorumluId(String hashId) {
        return sorumluRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.sorumlu.bulunamadi", hashId)))
                .getId();
    }
}
