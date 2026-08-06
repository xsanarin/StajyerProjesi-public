package com.example.StajEnvanterProjesi.security;

import com.example.StajEnvanterProjesi.handler.ApiError;
import com.example.StajEnvanterProjesi.handler.ExceptionDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;

/**
 * Kimligi olmayan istege 401 dondurur.
 *
 * NEDEN GEREKLI: formLogin ve httpBasic kapali oldugu icin (stateless JWT) Spring
 * Security varsayilan olarak Http403ForbiddenEntryPoint secer ve kimliksiz istege de
 * 403 doner. Bu, "token'in yok/bozuk/suresi dolmus" (401) ile "token'in gecerli ama
 * rolun yetmiyor" (403) durumlarini ayirt edilemez hale getiriyordu.
 *
 * Frontend (api/client.ts) tam olarak bu ayrima gore yazilmis: 401'de oturumu temizleyip
 * login'e atiyor, 403'te kullaniciyi yerinde birakip toast gosteriyor. 401 hic donmedigi
 * icin token 30 dk sonra doldugunda kullanici login'e atilmiyor, bunun yerine her sayfada
 * "Bu islem icin yetkiniz yok" goruyor ve localStorage elle temizlenmeden kurtulamiyordu.
 *
 * Yetkisiz (kimlik var, rol yetmiyor) durumu bu sinifa hic ugramaz; onu Spring'in
 * varsayilan AccessDeniedHandler'i 403 olarak dondurmeye devam eder.
 *
 * Govde GlobalExceptionHandler ile ayni sekilde uretilir ({ status, exception: {...} }),
 * cunku frontend'deki hataMesaji() mesaji exception.message alanindan okuyor.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Spring Boot 4 Jackson 3 kullanir: bean tipi tools.jackson.databind.ObjectMapper'dir,
    // eski com.fasterxml.jackson.databind.ObjectMapper diye bir bean YOKTUR (uygulama
    // "No qualifying bean of type ObjectMapper" ile ayaga kalkmaz).
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.UNAUTHORIZED.value());

        ExceptionDetail detail = new ExceptionDetail();
        // Sebep detayi (token suresi mi doldu, imza mi bozuk) client'a SIZDIRILMAZ.
        detail.setMessage("Oturum gecersiz veya suresi dolmus");
        detail.setCreateTime(new Date());
        detail.setPath(request.getRequestURI());

        apiError.setException(detail);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), apiError);
    }
}
