package com.example.StajEnvanterProjesi.handler;

import com.example.StajEnvanterProjesi.exception.BaseException;








import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Her BaseException turevi kendi HttpStatus'unu tasir; ayri handler yazmaya gerek yok.
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError> handleBaseException(BaseException ex, WebRequest request) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(createApiError(ex.getMessage(), request, ex.getHttpStatus()));
    }

    // @Valid ihlali (ADIM 2/4 DTO validasyonlari): istemci hatasi -> 400 (500 degil).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(createApiError(msg, request, HttpStatus.BAD_REQUEST));
    }

    // ADIM 4 - Gorev 4 sahiplik ihlali: sorumlu baskasinin stajina erisirse 403.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createApiError(ex.getMessage(), request, HttpStatus.FORBIDDEN));
    }

    // @RequestParam / @PathVariable uzerindeki (jakarta) constraint ihlalleri:
    // bunlar MethodArgumentNotValidException degil, ConstraintViolationException firlatir
    // (@Validated'li controller + param-seviyesi kisit). Yine istemci hatasi -> 400.
    // Mesaji @Valid handler'indaki gibi ihlalleri birlestirerek uretiriz.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String msg = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(createApiError(msg, request, HttpStatus.BAD_REQUEST));
    }

    // Bozuk/parse edilemeyen JSON govde: istemci hatasi -> 400 (500 degil).
    // Parse detayi (hangi karakter/alan) client'a SIZDIRILMAZ; genel mesaj doner.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(createApiError("Gecersiz istek govdesi", request, HttpStatus.BAD_REQUEST));
    }

    // Haritalanmamis URL: Spring NoResourceFoundException firlatir. Asagidaki genel
    // Exception handler'i bunu da yutup 500 dondurdugu icin ayri ele alinir -> gercek 404.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoResourceFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createApiError("Kaynak bulunamadi", request, HttpStatus.NOT_FOUND));
    }

    // Dogru URL, yanlis HTTP metodu (or. POST-only /api/auth/login'e GET). Istemci
    // hatasi -> 405. Ayri ele alinmazsa asagidaki genel handler yutup 500 dondurur ve
    // her seferinde log'a tam stack trace yazar.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                            WebRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(createApiError("Bu uc nokta bu HTTP metodunu desteklemiyor",
                        request, HttpStatus.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex, WebRequest request) {
        // Gercek hata detayi client'a sizdirilmaz; sadece loglanir.
        log.error("Beklenmedik bir hata olustu", ex);
        return ResponseEntity.internalServerError()
                .body(createApiError("Beklenmedik bir hata olustu", request, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private ApiError createApiError(Object message, WebRequest request, HttpStatus status) {
        ApiError apiError = new ApiError();
        apiError.setStatus(status.value());

        ExceptionDetail detail = new ExceptionDetail();
        detail.setMessage(message);
        detail.setCreateTime(new Date());
        detail.setPath(request.getDescription(false).substring(4));

        apiError.setException(detail);
        return apiError;
    }
}
