package com.example.StajEnvanterProjesi.config;

import com.example.StajEnvanterProjesi.entity.Kullanici;
import com.example.StajEnvanterProjesi.entity.enums.Rol;
import com.example.StajEnvanterProjesi.repository.KullaniciRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * ADIM 2 — Ilk admin hesabini olusturur (yoksa). Boylece uygulama ilk kez ayaga
 * kalktiginda login yapabilecegin bir ADMIN bulunur. Admin sifresini test kolayligi
 * icin dogrudan kullanir (sifreDegistirmeli=false).
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminSifre;

    public AdminSeeder(KullaniciRepository kullaniciRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email}") String adminEmail,
                       @Value("${app.admin.sifre}") String adminSifre) {
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminSifre = adminSifre;
    }

    @Override
    public void run(String... args) {
        if (kullaniciRepository.existsByEmail(adminEmail)) {
            return;
        }
        Kullanici admin = new Kullanici();
        admin.setEmail(adminEmail);
        admin.setSifreHash(passwordEncoder.encode(adminSifre));
        admin.setRol(Rol.ADMIN);
        admin.setSifreDegistirmeli(false);
        kullaniciRepository.save(admin);
    }
}
