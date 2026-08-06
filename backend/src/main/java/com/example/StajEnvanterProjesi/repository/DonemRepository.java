package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Donem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonemRepository extends JpaRepository<Donem, Long> {

    // Listeleme: soft-delete edilmis kayitlar gorunmemeli (bkz. StajRepository).
    List<Donem> findByStatusTrue();

    // Ad tekilligi: donem adi Yaz/Guz/Bahar ile sinirli oldugundan ayni ad iki kez
    // acilamaz. Silinmis kayitlar sayilmaz, yoksa ad bir daha kullanilamazdi.
    Optional<Donem> findByAdAndStatusTrue(String ad);

    // Aktif donemi bulmak icin. "Ayni anda tek aktif donem" kuralini
    // Service'te dogrularken bu metodu kullanacagiz.
    Optional<Donem> findByAktifTrue();

    // DTO kurali: disaridan hashId gelir, controller burada Long id'ye cevirir.
    Optional<Donem> findByHashId(String hashId);
}
