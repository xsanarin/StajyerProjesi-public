package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.GorevSablonu;
import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GorevSablonuRepository extends JpaRepository<GorevSablonu, Long> {

    Optional<GorevSablonu> findByIdAndAktifTrue(Long id);

    Optional<GorevSablonu> findByHashId(String hashId);

    // Oneri mekanizmasi: stajin departmani + seviyesine uyan aktif sablonlar.
    List<GorevSablonu> findByDepartmanIdAndSeviyeAndAktifTrue(Long departmanId, Seviye seviye);

    List<GorevSablonu> findByAktifTrue();

    // Admin listesi tek departmana kilitli: departman secilmeden liste gosterilmiyor.
    List<GorevSablonu> findByDepartmanId(Long departmanId);

    List<GorevSablonu> findByDepartmanIdAndAktifTrue(Long departmanId);
}
