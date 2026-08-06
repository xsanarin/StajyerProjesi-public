package com.example.StajEnvanterProjesi.entity.dto.Staj;

/**
 * Istatistik ekraninin tek ihtiyaci: departman basina BENZERSIZ stajyer sayisi.
 *
 * Bu sayiyi eskiden istemci tum staj listesini cekip Set ile hesapliyordu;
 * artik COUNT(DISTINCT) ile veritabani hesapliyor, tel uzerinden sadece
 * departman sayisi kadar satir geciyor.
 */
public record DepartmanIstatistikResponse(String departmanAd, Long stajyerSayisi) {
}
