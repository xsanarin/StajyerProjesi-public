package com.example.StajEnvanterProjesi.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ADIM 1+ (KISI-B) — Stajin haftalik devam gunleri <-> tek kolon metin.
 *
 * "MONDAY,WEDNESDAY,FRIDAY" gibi saklanir. Ayri bir tablo acmak yerine tek kolon:
 * bu veri her zaman stajla birlikte okunur, hicbir zaman tek basina sorgulanmaz.
 *
 * java.time.DayOfWeek kullaniliyor (Turkce bir enum yerine): LocalDate.getDayOfWeek()
 * ile dogrudan karsilastirilabiliyor, cevrim hatasi riski yok.
 * Siralama LinkedHashSet ile korunur, boylece DB'deki metin de okunabilir kalir.
 */
@Converter(autoApply = false)
public class DevamGunleriConverter implements AttributeConverter<Set<DayOfWeek>, String> {

    @Override
    public String convertToDatabaseColumn(Set<DayOfWeek> gunler) {
        if (gunler == null || gunler.isEmpty()) {
            return null;
        }
        // Hafta sirasina gore yaz: "MONDAY,WEDNESDAY,FRIDAY" (girdi sirasi ne olursa olsun).
        return gunler.stream()
                .sorted()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<DayOfWeek> convertToEntityAttribute(String metin) {
        if (metin == null || metin.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(metin.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(DayOfWeek::valueOf)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
