package com.example.StajEnvanterProjesi.repository;

import com.example.StajEnvanterProjesi.entity.Staj;
import com.example.StajEnvanterProjesi.entity.Stajyer;
import com.example.StajEnvanterProjesi.entity.dto.Staj.DepartmanIstatistikResponse;
import com.example.StajEnvanterProjesi.entity.enums.StajDurumu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StajRepository extends JpaRepository<Staj, Long> {

    // Listeleme: soft-delete edilmis (status=false) kayitlar gorunmemeli.
    // Duz findAll() onlari da donduruyordu — silinen staj listede kaliyordu.
    List<Staj> findByStatusTrue();

    // Gorev 5: unique kontrolunu Service'te onceden yapabilmek icin.
    boolean existsByStajyerIdAndDonemId(Long stajyerId, Long donemId);

    // Gorev 4 - YETKI IZOLASYONU (planin kalbi):
    // Bir sorumlu yalnizca asil VEYA yedek oldugu stajlari gorebilir.
    // Spring Data metod ismi bunu otomatik uretir:
    //   WHERE asil_sorumlu_id = ?  OR  yedek_sorumlu_id = ?
    List<Staj> findByAsilSorumluIdOrYedekSorumluId(Long asilSorumluId, Long yedekSorumluId);

    // Gorev 1 - DEVIR: bir asil silindiginde onun asil oldugu tum stajlar lazim.
    List<Staj> findByAsilSorumluId(Long asilSorumluId);

    // Ayni kisi + donem kaydini bulmak icin (opsiyonel yardimci).
    Optional<Staj> findByStajyerIdAndDonemId(Long stajyerId, Long donemId);

    // Disari acilan hashId -> ic Long id cevrimi (DTO kurali). ADIM 3/4 endpoint'leri kullanir.
    //
    // status = true SART: soft-delete edilmis staj disaridan erisilemez olmali. Filtresiz
    // findByHashId, hashId'yi bilen istemcinin silinmis staji puanlamasina / kilidini
    // acmasina / gorev maddesine dokunmasina izin veriyordu (silme aninda sorumlunun
    // ekrani acik kalirsa "puanla" istegi geciyor, staj TAMAMLANDI'ya cekiliyor ve
    // stajyere kapanis maili gidiyordu).
    //
    // Kontrol tek tek servislerde DEGIL burada: eskiden her cagri yerinin ardindan elle
    // status bakmak gerekiyordu ve 15 yerin 14'unde unutulmustu. Sorguya gomulunce
    // unutulacak bir sey kalmiyor.
    //
    // delete() bu metodu kullanmaz (findById uzerinden calisir), silme akisi etkilenmez.
    // Ileride "arsiv/silinenler" ekrani istenirse filtresiz bir metot BILINCLI olarak
    // eklenir; bugunku sorun erisimin kazara acik olmasiydi.
    Optional<Staj> findByHashIdAndStatusTrue(String hashId);

    // ADIM 2 (KISI-B) - /api/me/stajlar: stajyerin kendi stajlari, GECMIS DONEMLER DAHIL.
    // Durum filtresi yok; tamamlanmis staj salt-okunur gorunur ama gorunur kalir.
    List<Staj> findByStajyerIdOrderByIdDesc(Long stajyerId);

    /**
     * ADIM 3 (KISI-B) — Bitisi yaklasan stajlar, opsiyonel arama ile.
     *
     * Eski "gun kalan" hesabi Stajyer.durum (String) + Stajyer.stajBitisTarihi uzerindeydi;
     * ADIM 0'da o alanlar Staj'a tasindi ve durum enum oldu. Sorgu artik burada.
     * Tarih araligi ICE DAHIL: bugun biten de, N gun sonra biten de listeye girer.
     *
     * status = true SART: silme soft-delete (StajServiceImpl.delete yalnizca
     * status'u false yapar, durum'a DOKUNMAZ). Yani silinen staj sonsuza kadar
     * DEVAM_EDIYOR kalir ve bitis tarihi de yerinde durur; filtre olmadan
     * silinmis stajyer icin sorumluya her gun "puanlamayi unutma" maili gidiyor,
     * ustelik sorumlu o staji ekranda bulamiyordu (diger listeler status suzuyor).
     *
     * Arama alanlari ekranda gorunen kolonlarla ayni: stajyer, departman, asil ve
     * yedek sorumlu. Kalip uretimi ve "neden null degil %%" gerekcesi icin
     * {@link com.example.StajEnvanterProjesi.entity.dto.SayfaResponse#benzerlik}.
     * Bos aramada stajyer ad/soyad zorunlu oldugu icin tum satirlar eslesir.
     *
     * LEFT JOIN FETCH: toResponse ve mail govdesi her satirda stajyer/departman/
     * sorumlu okuyor, fetch olmadan N+1 SELECT gidiyordu.
     */
    @Query("""
            SELECT s FROM staj s
              LEFT JOIN FETCH s.stajyer st
              LEFT JOIN FETCH s.departman dp
              LEFT JOIN FETCH s.asilSorumlu a
              LEFT JOIN FETCH s.yedekSorumlu y
            WHERE s.status = true
              AND s.durum = :durum
              AND s.stajBitisTarihi BETWEEN :baslangic AND :bitis
              AND (staj_katla(CONCAT(st.ad, ' ', st.soyad)) LIKE staj_katla(:q)
                   OR staj_katla(dp.ad) LIKE staj_katla(:q)
                   OR staj_katla(CONCAT(a.ad, ' ', a.soyad)) LIKE staj_katla(:q)
                   OR staj_katla(CONCAT(y.ad, ' ', y.soyad)) LIKE staj_katla(:q))
            ORDER BY s.stajBitisTarihi ASC
            """)
    List<Staj> bitisiYaklasanlar(@Param("durum") StajDurumu durum,
                                 @Param("baslangic") LocalDate baslangic,
                                 @Param("bitis") LocalDate bitis,
                                 @Param("q") String q);

    // ADIM 3 - TERFI: sadece DEVAM_EDIYOR stajlar terfi/bosalma islemine girer.
    List<Staj> findByAsilSorumluIdAndDurum(Long asilSorumluId, StajDurumu durum);

    List<Staj> findByYedekSorumluIdAndDurum(Long yedekSorumluId, StajDurumu durum);

    /**
     * SAYFALI ARAMA — admin staj listesinin tek okuma yolu.
     *
     * Neden: findByStatusTrue() tum kayitlari cekiyordu, arama da istemcideydi.
     * 25 stajda sorun degil; 2.000 stajda hem tel hem tarayici bogulur. Filtreleme
     * ve LIMIT/OFFSET artik Postgres'te.
     *
     * Bos birakilabilen filtreler ":param IS NULL OR ..." ile opsiyonel: tek sorgu
     * hepsini karsiliyor, kombinasyon basina ayri metot yazmiyoruz.
     *
     * LEFT JOIN FETCH sart: @ManyToOne varsayilani EAGER, fetch olmadan her satir
     * icin ayri SELECT gider (N+1). ToOne fetch + sayfalama guvenli — koleksiyon
     * fetch'inde oldugu gibi satir cogalmasi olmadigi icin LIMIT dogru calisir.
     *
     * Kucultme iki tarafta da SQL'de yapiliyor; Java'da kucultup DB'de aramak
     * "İstanbul" gibi kelimelerde eslesmeyi bozuyordu. Duz LOWER() de yetmiyor:
     * bu veritabani C collation ile kurulu, LOWER yalnizca ASCII'yi katliyor ve
     * kucuk harfli "sahin" aramasi "Sahin" kaydini kaciriyordu. Katlama artik
     * staj_katla() fonksiyonunda — gerekcesi V18__turkce_arama_katlama.sql'de.
     */
    @Query(value = """
            SELECT s FROM staj s
              LEFT JOIN FETCH s.stajyer st
              LEFT JOIN FETCH s.donem d
              LEFT JOIN FETCH s.departman dp
              LEFT JOIN FETCH s.asilSorumlu a
              LEFT JOIN FETCH s.yedekSorumlu y
            WHERE s.status = true
              AND (:departmanId IS NULL OR dp.id = :departmanId)
              AND (:durum IS NULL OR s.durum = :durum)
              AND (staj_katla(CONCAT(st.ad, ' ', st.soyad)) LIKE staj_katla(:q)
                   OR staj_katla(st.email) LIKE staj_katla(:q)
                   OR staj_katla(d.ad) LIKE staj_katla(:q)
                   OR staj_katla(dp.ad) LIKE staj_katla(:q)
                   OR staj_katla(CONCAT(a.ad, ' ', a.soyad)) LIKE staj_katla(:q)
                   OR staj_katla(CONCAT(y.ad, ' ', y.soyad)) LIKE staj_katla(:q))
            """,
            countQuery = """
            SELECT COUNT(s) FROM staj s
              LEFT JOIN s.stajyer st
              LEFT JOIN s.donem d
              LEFT JOIN s.departman dp
              LEFT JOIN s.asilSorumlu a
              LEFT JOIN s.yedekSorumlu y
            WHERE s.status = true
              AND (:departmanId IS NULL OR dp.id = :departmanId)
              AND (:durum IS NULL OR s.durum = :durum)
              AND (staj_katla(CONCAT(st.ad, ' ', st.soyad)) LIKE staj_katla(:q)
                   OR staj_katla(st.email) LIKE staj_katla(:q)
                   OR staj_katla(d.ad) LIKE staj_katla(:q)
                   OR staj_katla(dp.ad) LIKE staj_katla(:q)
                   OR staj_katla(CONCAT(a.ad, ' ', a.soyad)) LIKE staj_katla(:q)
                   OR staj_katla(CONCAT(y.ad, ' ', y.soyad)) LIKE staj_katla(:q))
            """)
    Page<Staj> ara(@Param("q") String q,
                   @Param("departmanId") Long departmanId,
                   @Param("durum") StajDurumu durum,
                   Pageable pageable);

    /**
     * V22 — Bir departmanda staj yapan stajyerler; BT talep dialogundaki kisi
     * seciminin kaynagi.
     *
     * Departman kisiye degil STAJA bagli oldugu icin "departmanin stajyerleri"
     * ancak staj uzerinden sorulabilir. DISTINCT sart: ayni kisi ayni departmanda
     * birden fazla donem staj yapmis olabilir, listede bir kere gorunmeli.
     *
     * Iki status kontrolu de gerekli ve ayri seyleri suzer: s.status silinmis
     * STAJI, st.status silinmis KISIYI eler. Ikincisi olmadan, silinen bir
     * stajyer eski staj kaydi uzerinden secim listesinde durmaya devam ederdi.
     *
     * DURUM filtresi (urun karari): yalnizca staji DEVAM EDEN stajyerler listelenir.
     * Bitmis/iptal stajin sahibi cikmaz — talep acilan kisi o an departmanda fiilen
     * calisiyor olmali. Bu yuzden donem filtresine ayrica gerek yok: gecmis donem
     * stajlari zaten TAMAMLANDI.
     *
     * status ve durum ayni sey DEGIL: status kaydin silinip silinmedigi,
     * durum stajin devam edip etmedigi. Ikisi de gerekli.
     */
    @Query("""
            SELECT DISTINCT st FROM staj s
              JOIN s.stajyer st
            WHERE s.status = true
              AND st.status = true
              AND s.durum = :durum
              AND s.departman.id = :departmanId
            ORDER BY st.ad ASC, st.soyad ASC
            """)
    List<Stajyer> departmanStajyerleri(@Param("departmanId") Long departmanId,
                                       @Param("durum") StajDurumu durum);

    /**
     * V22 — "Bu stajyerin bu departmanda su durumda staji var mi?" Talep atamasinin
     * dogrulamasi: istemci listeyi filtrelese de istek elle degistirilebilir.
     *
     * Filtreleri ustteki departmanStajyerleri ile AYNI olmak zorunda (ikisi de
     * silinmemis + verilen durum): kapsamlar ayrilirsa dialogda gorunen bir kisi
     * kaydederken reddedilir ya da gorunmeyen biri kabul edilirdi. Cagiran taraf
     * ikisine de ayni durumu verir (TalepServiceImpl.TALEP_ICIN_GECERLI_DURUM).
     */
    boolean existsByStajyerIdAndDepartmanIdAndDurumAndStatusTrue(Long stajyerId,
                                                                 Long departmanId,
                                                                 StajDurumu durum);

    /**
     * Istatistik ekrani: departman basina BENZERSIZ stajyer sayisi.
     *
     * "Kac stajyer" sorusunun cevabi oldugu icin staj degil stajyer sayiyoruz —
     * ayni kisi iki donem ayni departmanda staj yapmis olabilir, bir kere sayilir.
     * Istemcideki Set mantiginin SQL karsiligi.
     */
    @Query("""
            SELECT new com.example.StajEnvanterProjesi.entity.dto.Staj.DepartmanIstatistikResponse(
                       dp.ad, COUNT(DISTINCT st.id))
            FROM staj s
              JOIN s.departman dp
              JOIN s.stajyer st
            WHERE s.status = true
              AND (:durum IS NULL OR s.durum = :durum)
            GROUP BY dp.ad
            ORDER BY COUNT(DISTINCT st.id) DESC, dp.ad ASC
            """)
    List<DepartmanIstatistikResponse> departmanaGoreStajyerSayisi(@Param("durum") StajDurumu durum);
}
