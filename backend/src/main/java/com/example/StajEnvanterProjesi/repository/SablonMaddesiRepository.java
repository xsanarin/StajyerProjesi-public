package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.SablonMaddesi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SablonMaddesiRepository extends JpaRepository<SablonMaddesi, Long> {

    Optional<SablonMaddesi> findByHashId(String hashId);

    // Yeni madde eklenirken sonraki sira numarasini belirlemek icin.
    Optional<SablonMaddesi> findFirstBySablonIdOrderBySiraDesc(Long sablonId);
}
