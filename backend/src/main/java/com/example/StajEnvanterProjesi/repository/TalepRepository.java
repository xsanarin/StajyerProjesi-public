package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Talep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TalepRepository extends JpaRepository<Talep, Long> {

    /**
     * Silinmis talep hicbir kapidan gorunmemeli. Filtresiz findByHashId BILEREK
     * eklenmedi: Staj tarafinda filtresiz metot 15 cagri yerinin 14'unde yanlis
     * kullanilmisti (bkz. StajRepository.findByHashIdAndStatusTrue yorumu).
     */
    Optional<Talep> findByHashIdAndStatusTrue(String hashId);

    /** Admin listesi: tum aktif talepler, yenisi ustte. */
    List<Talep> findByStatusTrueOrderByIdDesc();

    /** Sorumlu listesi: yalnizca kendi departmaninin talepleri. */
    List<Talep> findByDepartmanIdAndStatusTrueOrderByIdDesc(Long departmanId);
}
