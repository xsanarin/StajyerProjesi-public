package com.example.StajEnvanterProjesi.config;

import com.example.StajEnvanterProjesi.service.BildirimService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "app.bildirim.zamanlanmis", havingValue = "true", matchIfMissing = true)
public class BitisBildirimJob {

    private static final Logger log = LoggerFactory.getLogger(BitisBildirimJob.class);

    private final BildirimService bildirimService;

    public BitisBildirimJob(BildirimService bildirimService) {
        this.bildirimService = bildirimService;
    }

    @Scheduled(cron = "0 0 8 * * MON-FRI", zone = "Europe/Istanbul")
    public void bitisiYaklasanlariBildir() {
        try {
            int gonderilen = bildirimService.bitisiYaklasanlariBildir();
            log.info("Zamanlanmis bitis bildirimi tamamlandi ({} sorumlu).", gonderilen);
        } catch (Exception ex) {
            // Job icinden kacan istisna zamanlayiciyi susturabilir; burada yakaliyoruz.
            log.error("Zamanlanmis bitis bildirimi basarisiz: {}", ex.getMessage(), ex);
        }
    }
}
