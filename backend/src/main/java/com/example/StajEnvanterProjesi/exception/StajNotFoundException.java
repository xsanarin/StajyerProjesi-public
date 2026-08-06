package com.example.StajEnvanterProjesi.exception;

import org.springframework.http.HttpStatus;

// Ornek: BaseException'dan tureyen ve 404 (NOT_FOUND) tasiyan exception.
public class StajNotFoundException extends BaseException {

    public StajNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}