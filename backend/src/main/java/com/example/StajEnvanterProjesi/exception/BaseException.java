package com.example.StajEnvanterProjesi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {

    private final HttpStatus httpStatus;

    // Geriye donuk uyumlu: eski cagrimlar varsayilan olarak BAD_REQUEST (400) doner.
    public BaseException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    // Her exception turevi kendi HTTP status kodunu tasiyabilir.
    public BaseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}