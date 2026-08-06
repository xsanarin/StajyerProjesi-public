package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Sorumlu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SorumluRepository extends JpaRepository<Sorumlu, Long> {

    // Listeleme: soft-delete edilmis (ayrilmis) sorumlular gorunmemeli.
    // Bkz. StajRepository — ayrilan sorumlu, staj acma listesine dusmemeli.
    List<Sorumlu> findByStatusTrue();

    // DTO kurali: disaridan hashId gelir, controller burada Long id'ye cevirir.
    Optional<Sorumlu> findByHashId(String hashId);
}