package com.example.StajEnvanterProjesi.entity.dto.Departman;

import lombok.Data;

/**
 * Disariya id yerine hashId veriyoruz (DTO kurali - ADIM 0).
 */
@Data
public class DepartmanResponse {
    private String hashId;
    private String ad;
    private Boolean status;
}