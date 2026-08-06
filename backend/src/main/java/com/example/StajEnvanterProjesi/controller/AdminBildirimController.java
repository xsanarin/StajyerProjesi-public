package com.example.StajEnvanterProjesi.controller;

import com.example.StajEnvanterProjesi.entity.dto.Bildirim.YaklasanBitisResponse;
import com.example.StajEnvanterProjesi.service.BildirimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ADIM 3 (KISI-B) — Admin gorunumu ve job'in elle tetiklenmesi.
 *
 * Bu, eski `GET /stajyerApplication/gunKalan` ucunun yerini alir. Eskisi
 * Stajyer.durum (String) ve Stajyer.stajBitisTarihi'ne dayaniyordu; ADIM 0'da
 * iki alan da Staj'a tasinince metod bos liste dondurur hale gelmisti.
 */
@RestController
@RequestMapping("/api/admin/bildirim")
public class AdminBildirimController {

    private final BildirimService bildirimService;

    public AdminBildirimController(BildirimService bildirimService) {
        this.bildirimService = bildirimService;
    }

    /**
     * Tum departmanlarda bitisi yaklasan stajlar.
     *
     * q: stajyer, departman ya da sorumlu adinda arar; verilmezse suzme yok.
     */
    @GetMapping("/yaklasan-bitisler")
    public ResponseEntity<List<YaklasanBitisResponse>> yaklasanBitisler(
            @RequestParam(defaultValue = "5") int gun,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(bildirimService.yaklasanBitisler(gun, q));
    }

    /**
     * Gunluk job'i elle tetikler — 08:00'i beklemeden test/dogrulama icin.
     * Mail gonderimi app.bildirim.mail.gonder ile kontrol edilir; kapaliysa
     * gonderilecek icerik log'a yazilir ve bildirilen=0 doner.
     */
    @PostMapping("/tetikle")
    public ResponseEntity<Map<String, Object>> tetikle() {
        int gonderilen = bildirimService.bitisiYaklasanlariBildir();
        return ResponseEntity.ok(Map.of("bildirilenSorumluSayisi", gonderilen));
    }
}
