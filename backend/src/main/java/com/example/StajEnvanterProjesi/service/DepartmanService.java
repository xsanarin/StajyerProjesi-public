package com.example.StajEnvanterProjesi.service;

import com.example.StajEnvanterProjesi.entity.dto.Departman.DepartmanRequest;
import com.example.StajEnvanterProjesi.entity.dto.Departman.DepartmanResponse;

import java.util.List;

/**
 * ADIM 1 — Departman is kurallarinin sozlesmesi (arayuz).
 * Controller sadece bu arayuzu bilir; gercek is DepartmanServiceImpl'de yapilir.
 * Donem modulu ile ayni CRUD kalibini izler.
 */
public interface DepartmanService {

    DepartmanResponse findById(Long id);

    List<DepartmanResponse> findAll();

    DepartmanResponse save(DepartmanRequest request);

    DepartmanResponse update(Long id, DepartmanRequest request);

    void delete(Long id);
}