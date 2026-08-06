package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.StajGorev;
import com.example.StajEnvanterProjesi.entity.enums.GorevDurumu;
import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StajGorevRepository extends JpaRepository<StajGorev, Long> {

    Optional<StajGorev> findByHashId(String hashId);

    List<StajGorev> findByStajIdOrderByAtanmaTarihiAsc(Long stajId);

    // Seviye yukseltme kurali: mevcut seviyede bitmemis gorev var mi?
    boolean existsByStajIdAndSeviyeAndDurumNot(Long stajId, Seviye seviye, GorevDurumu durum);

    boolean existsByStajIdAndSeviye(Long stajId, Seviye seviye);

    // Cift atama korumasi: ayni sablon ayni staja iki kez atanmasin (cift tiklama).
    boolean existsByStajIdAndKaynakSablonId(Long stajId, Long kaynakSablonId);
}
