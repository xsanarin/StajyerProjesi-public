package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.dto.SayfaResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.DepartmanIstatistikResponse;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajGuncelleRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajRequest;
import com.example.StajEnvanterProjesi.entity.dto.Staj.StajResponse;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.repository.StajRepository;
import com.example.StajEnvanterProjesi.service.StajService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * DTO kurali (ADIM 0): disariya sadece hashId acilir. Uc noktalar hashId alir,
 * Long id'ye cevrimi bu katman yapar; servisler iceride Long id ile calisir.
 */
@RestController
@RequestMapping("/staj")
public class StajController {

    @Autowired
    private StajService stajService;

    @Autowired
    private StajRepository stajRepository;

    @Autowired
    private SorumluRepository sorumluRepository;

    @Autowired
    private MessageHelper messageHelper;

    @GetMapping("/getById/{hashId}")
    public ResponseEntity<StajResponse> getById(@PathVariable("hashId") String hashId) {
        StajResponse response = stajService.findById(stajId(hashId));
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * DIKKAT: sayfalama yok, tum stajlari doner. Liste ekranlari /staj/ara
     * kullanmali. Acilir liste / disa aktarma gibi kucuk kullanimlar icin duruyor.
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<StajResponse>> getAll() {
        return ResponseEntity.ok(stajService.findAll());
    }

    /**
     * Sayfali + aramali staj listesi.
     *
     * Ornek: /staj/ara?q=ahmet&durum=DEVAM_EDIYOR&sayfa=0&boyut=25
     * Bos birakilan filtre "hepsi" demek. boyut en fazla 100'e kirpilir.
     */
    @GetMapping("/ara")
    public ResponseEntity<SayfaResponse<StajResponse>> ara(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String departmanHashId,
            @RequestParam(required = false) StajDurumu durum,
            @RequestParam(defaultValue = "0") int sayfa,
            @RequestParam(defaultValue = "25") int boyut) {
        return ResponseEntity.ok(stajService.ara(q, departmanHashId, durum, sayfa, boyut));
    }

    /**
     * Istatistik ekraninin ozet ucu: departman basina benzersiz stajyer sayisi.
     * Eskiden istemci tum listeyi cekip hesapliyordu; artik SQL hesapliyor.
     */
    @GetMapping("/istatistik/departman")
    public ResponseEntity<List<DepartmanIstatistikResponse>> departmanIstatistigi(
            @RequestParam(defaultValue = "true") boolean sadeceAktif) {
        return ResponseEntity.ok(stajService.departmanIstatistigi(sadeceAktif));
    }

    // Admin: yeni staj ac (stajyer + asil + yedek + level)
    @PostMapping("/save")
    public ResponseEntity<StajResponse> save(@Valid @RequestBody StajRequest request) {
        return ResponseEntity.ok(stajService.save(request));
    }

    /**
     * Gorev 4: "sadece kendi stajyerlerim".
     * SIMDILIK sorumluyu query param ile aliyoruz ki guvenlik olmadan test edebilelim.
     * Faz 1 guvenlik adiminda bu kimlik JWT'den gelecek:
     *   Long sorumluId = SecurityContextHolder.getContext()... ile alinacak, param kalkacak.
     */
    @GetMapping("/benim")
    public ResponseEntity<List<StajResponse>> benimStajlarim(@RequestParam String sorumluHashId) {
        return ResponseEntity.ok(stajService.benimStajlarim(sorumluId(sorumluHashId)));
    }

    // Admin: acilmis stajin tarihlerini / devam planini / asil sorumlusunu duzelt.
    @PutMapping("/update/{hashId}")
    public ResponseEntity<StajResponse> update(@PathVariable("hashId") String hashId,
                                               @Valid @RequestBody StajGuncelleRequest request) {
        return ResponseEntity.ok(stajService.guncelle(hashId, request));
    }

    @DeleteMapping("/delete/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable("hashId") String hashId) {
        stajService.delete(stajId(hashId));
        return ResponseEntity.ok().build();
    }

    // ---- hashId -> Long id cevrimi ----

    private Long stajId(String hashId) {
        return stajRepository.findByHashIdAndStatusTrue(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.staj.bulunamadi")))
                .getId();
    }

    private Long sorumluId(String hashId) {
        return sorumluRepository.findByHashId(hashId)
                .orElseThrow(() -> new BaseException(messageHelper.get("error.sorumlu.bulunamadi", hashId)))
                .getId();
    }
}