package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Stajyer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StajyerRepository extends JpaRepository<Stajyer, Long> {
        Optional<Stajyer> findByEmailAndStatusTrue(String email);

        // Listeleme: soft-delete edilmis kayitlar gorunmemeli (bkz. StajRepository).
        List<Stajyer> findByStatusTrue();

        // Disari acilan hashId ile ic Long id'ye cevirmek icin (DTO kurali - ADIM 0).
        Optional<Stajyer> findByHashId(String hashId);

        // NOT: findByDurumAndStatusTrueAndStajBitisTarihiBetween ADIM 0'da kaldirildi;
        // "bitise gun kalan" sorgusu artik Staj uzerinden yapilacak (ADIM 5).

        /**
         * SAYFALI ARAMA — stajyer listesi de sinirsiz buyuyen listelerden biri.
         * Gerekcesi ve katlama tercihi StajRepository.ara ile ayni.
         *
         * Stajyer'in iliskisi yok, o yuzden JOIN FETCH gerekmiyor; sayim
         * sorgusunu da Spring Data kendisi uretebiliyor.
         *
         * Sinif sayi ama kullanici aramaya "3" yazabiliyor: metne cevirip
         * karsilastiriyoruz (istemcideki sinif?.toString() ile ayni davranis).
         * Sayida katlanacak harf olmadigi icin orada duz LIKE kaliyor.
         */
        @Query("""
                SELECT s FROM stajyer s
                WHERE s.status = true
                  AND (staj_katla(CONCAT(s.ad, ' ', s.soyad)) LIKE staj_katla(:q)
                       OR staj_katla(s.email) LIKE staj_katla(:q)
                       OR staj_katla(s.telefon) LIKE staj_katla(:q)
                       OR staj_katla(s.universite) LIKE staj_katla(:q)
                       OR staj_katla(s.bolum) LIKE staj_katla(:q)
                       OR CAST(s.sinif AS string) LIKE :q)
                """)
        Page<Stajyer> ara(@Param("q") String q, Pageable pageable);
}
