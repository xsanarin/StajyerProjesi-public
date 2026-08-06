package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    // Login: aktif hesabi e-posta ile bul.
    Optional<Kullanici> findByEmailAndStatusTrue(String email);

    // Her istekte JwtAuthFilter'in yaptigi "hesap hala aktif mi?" kontrolu icin.
    Optional<Kullanici> findByIdAndStatusTrue(Long id);

    // E-posta tekilligi (provisioning).
    boolean existsByEmail(String email);

    /**
     * E-posta tekilligi (profil guncelleme). Kisinin KENDI hesabi disarida birakilir;
     * yoksa kisi kendi e-postasini degistirmeden kaydettiginde "bu e-posta zaten var"
     * hatasi alirdi.
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    // ADIM 3: sorumlu ayrilinca ilgili login hesabini kilitlemek icin.
    Optional<Kullanici> findBySorumluIdAndStatusTrue(Long sorumluId);

    // Profil guncellemesinde e-postayi login hesabiyla senkron tutmak icin.
    Optional<Kullanici> findByStajyerIdAndStatusTrue(Long stajyerId);
}
