package com.example.StajEnvanterProjesi.entity.dto.Departman;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmanRequest {
    @NotBlank
    private String ad;
}