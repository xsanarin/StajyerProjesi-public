package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Degerlendirme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DegerlendirmeRepository extends JpaRepository<Degerlendirme, Long> {

    // 1 staj = 1 puan kuralinin servis tarafi kontrolu.
    boolean existsByStajId(Long stajId);

    // Admin duzeltme / okuma icin stajin degerlendirmesi.
    Optional<Degerlendirme> findByStajId(Long stajId);
}
