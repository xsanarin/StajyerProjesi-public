package com.example.StajEnvanterProjesi.entity.dto.Talep;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Disariya id yerine hashId (DTO kurali - ADIM 0).
 * Departman adi da doner: sorumlu ekrani zaten tek departman gosterse de admin
 * listesinde her satirin hangi departmana ait oldugu gorunmeli.
 */
@Data
public class TalepResponse {

    private String hashId;
    private String departmanHashId;
    private String departmanAd;

    /**
     * Talebin acildigi stajyer (V22). Ikisi de null olabilir: departman geneline
     * acilmis talepte kimse atanmamistir.
     */
    private String stajyerHashId;
    private String stajyerAdSoyad;

    private String baslik;
    private String aciklama;

    /** Talebin ne zaman olusturuldugu; listede siralama/okuma icin. */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Europe/Istanbul")
    private LocalDateTime olusturmaTarihi;
}
