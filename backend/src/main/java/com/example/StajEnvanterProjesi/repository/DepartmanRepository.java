package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Departman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmanRepository extends JpaRepository<Departman, Long> {

    // Listeleme: soft-delete edilmis kayitlar gorunmemeli (bkz. StajRepository).
    List<Departman> findByStatusTrue();

    // Disari acilan hashId -> ic Long id cevrimi (DTO kurali).
    Optional<Departman> findByHashId(String hashId);

    // Silinmis departmana yeni kayit baglanmamali. Mevcut cagri yerleri ustteki
    // filtresiz metodu kullaniyor; yeni kod (BT talep) bunu kullanir.
    Optional<Departman> findByHashIdAndStatusTrue(String hashId);

    // Ad tekilligi kontrolu icin.
    boolean existsByAd(String ad);
}