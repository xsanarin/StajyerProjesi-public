package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Devam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DevamRepository  extends JpaRepository<Devam, Long> {

    // Upsert (giris/manuel): (staj, tarih) unique satirini STATU FARK ETMEKSIZIN bulur;
    // soft-delete edilmis satir varsa yeniden aktiflestirmek icin gerekir (yeni INSERT unique'i ihlal ederdi).
    Optional<Devam> findByStajIdAndTarih(Long stajId, LocalDate tarih);

    // Okuma: sadece aktif (status=true) kayitlar; soft-delete edilenler gorunmez.
    Optional<Devam> findByStajIdAndTarihAndStatusTrue(Long stajId, LocalDate tarih);
    List<Devam> findByStajIdAndTarihBetweenAndStatusTrue(Long stajId, LocalDate baslangic, LocalDate bitis);

    // Admin sil/duzelt: disaridan gelen devam hashId'siyle tekil AKTIF kaydi bul.
    Optional<Devam> findByHashIdAndStatusTrue(String hashId);
}
