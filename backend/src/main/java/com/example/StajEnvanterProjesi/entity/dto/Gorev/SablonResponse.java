package com.example.StajEnvanterProjesi.entity.dto.Gorev;

import com.example.StajEnvanterProjesi.entity.enums.Seviye;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SablonResponse {

    private String hashId;
    private String ad;
    private String aciklama;
    private String departmanHashId;
    private String departmanAd;
    private Seviye seviye;
    private Boolean aktif;
    private List<SablonMaddeResponse> maddeler = new ArrayList<>();
}
