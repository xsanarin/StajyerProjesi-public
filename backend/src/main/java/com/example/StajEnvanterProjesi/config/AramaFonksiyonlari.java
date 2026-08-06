package com.example.StajEnvanterProjesi.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

/**
 * Veritabanindaki staj_katla(text) fonksiyonunu HQL'e tanitir.
 *
 * NEDEN GEREKLI: JPQL'in tasinabilir FUNCTION('staj_katla', x) yazimi Hibernate
 * icin donus tipi BILINMEYEN bir cagri. Hibernate 7'de bu, sonuc bir string
 * baglamina girdiginde ayaga kalkarken hataya donuyor:
 *   "Operand of 'like' is of type 'java.lang.Object' which is not a string"
 * Fonksiyonu donus tipiyle kaydedince sorgularda dogrudan staj_katla(x)
 * yazilabiliyor ve LIKE'in iki tarafi da string olarak dogrulaniyor.
 *
 * Kayit META-INF/services/org.hibernate.boot.model.FunctionContributor ile
 * yapiliyor -- bu SPI Spring bean'i degil, Hibernate onu kendi ServiceLoader'i
 * ile buluyor. @Component eklemek ISE YARAMAZ.
 *
 * Fonksiyonun kendisi ve Turkce harf katlamasinin gerekcesi:
 * db/migration/V18__turkce_arama_katlama.sql
 */
public class AramaFonksiyonlari implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions katkilar) {
        katkilar.getFunctionRegistry().registerPattern(
                "staj_katla",
                "staj_katla(?1)",
                katkilar.getTypeConfiguration()
                        .getBasicTypeRegistry()
                        .resolve(StandardBasicTypes.STRING));
    }
}
