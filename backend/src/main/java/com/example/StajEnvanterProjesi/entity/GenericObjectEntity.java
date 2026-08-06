package com.example.StajEnvanterProjesi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@MappedSuperclass
public class GenericObjectEntity implements Serializable {

    /**
     * ADIM 0 — DTO kurali: DISARIYA sadece hashId, ICERIDE Long id.
     * URL/response'larda tahmin edilebilir sirali id ("/staj/3") yerine bu UUID kullanilir.
     * @PrePersist'te otomatik uretilir; updatable=false -> bir kere yazilir, degismez.
     * Mevcut satirlar icin backfill: src/main/resources/db/backfill_hash_id.sql
     */
    @Column(name = "hash_id", unique = true, updatable = false, length = 36)
    private String hashId;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss",timezone = "Europe/Istanbul")
    @DateTimeFormat(pattern = "dd/MM/yy HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private LocalDateTime createDate;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss",timezone = "Europe/Istanbul")
    @DateTimeFormat(pattern = "dd/MM/yy HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @JsonIgnore
    @Column(name = "status")
    private Boolean status = true;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        updateDate = LocalDateTime.now();
        if (hashId == null) {
            hashId = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }

}
