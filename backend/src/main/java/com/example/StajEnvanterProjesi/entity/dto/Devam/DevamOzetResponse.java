package com.example.StajEnvanterProjesi.entity.dto.Devam;

import lombok.Data;

/**
 * ADIM 1+ (KISI-B) — Aylik devam ozeti.
 *
 * Haftalik plan eklenmeden once bu hesap YAPILAMIYORDU: sistem gelinen gunleri
 * biliyordu ama gelinmesi GEREKEN gunleri bilmiyordu.
 *
 * Sayimlar yalnizca GECMIS planli gunleri kapsar — bugun ve sonrasi "devamsiz"
 * sayilmaz, yoksa ayin basinda herkes devamsiz gorunurdu.
 */
@Data
public class DevamOzetResponse {

    private int planliGun;      // gecmisteki planli gun sayisi (beklenen)
    private int gelinenGun;     // bu planli gunlerden ONAYLI kaydi olanlar
    private int devamsizGun;    // planliGun - gelinenGun
    private int planDisiGun;    // plan disi gelinen gunler (telafi/fazladan)
    private int devamYuzdesi;   // gelinenGun / planliGun (planliGun=0 ise 0)

    // V15 — stajyerin bildirdigi ama sorumlunun henuz onaylamadigi gunler.
    // devamsizGun icinde sayilirlar; onaylanirlarsa gelinenGun'e gecerler.
    private int onayBekleyenGun;
}
