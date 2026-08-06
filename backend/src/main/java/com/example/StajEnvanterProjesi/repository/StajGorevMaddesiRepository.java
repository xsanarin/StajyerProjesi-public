package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.StajGorevMaddesi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StajGorevMaddesiRepository extends JpaRepository<StajGorevMaddesi, Long> {

    Optional<StajGorevMaddesi> findByHashId(String hashId);

    List<StajGorevMaddesi> findByStajGorevIdOrderBySiraAsc(Long stajGorevId);

    // V23: ozel madde eklenirken sonraki sira numarasi.
    Optional<StajGorevMaddesi> findFirstByStajGorevIdOrderBySiraDesc(Long stajGorevId);
}
