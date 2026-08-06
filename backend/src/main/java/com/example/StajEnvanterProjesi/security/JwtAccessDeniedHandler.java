package com.example.StajEnvanterProjesi.security;

import com.example.StajEnvanterProjesi.handler.ApiError;
import com.example.StajEnvanterProjesi.handler.ExceptionDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;

/**
 * Kimlik var ama rol yetmiyor -> 403.
 *
 * NEDEN VARSAYILAN YETMIYOR: Spring'in AccessDeniedHandlerImpl'i response.sendError(403)
 * cagirir. sendError, servlet konteynerinde /error'a IKINCI bir dispatch baslatir; guvenlik
 * zinciri bu dispatch'te bastan calisir ve orada SecurityContext bostur. Sonuc: istek anonim
 * gorunur, JwtAuthenticationEntryPoint devreye girer ve 403'un uzerine 401 yazar. Yani rol
 * yetmemesi "oturumun dusmus" gibi raporlanir ve frontend kullaniciyi bosuna login'e atar.
 *
 * Burada sendError yerine status + govde dogrudan yazilir; error dispatch'i hic tetiklenmez,
 * 403 oldugu gibi istemciye ulasir.
 *
 * Govde GlobalExceptionHandler ile ayni sekilde uretilir ({ status, exception: {...} }),
 * cunku frontend'deki hataMesaji() mesaji exception.message alanindan okuyor.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.FORBIDDEN.value());

        ExceptionDetail detail = new ExceptionDetail();
        detail.setMessage("Bu islem icin yetkiniz yok");
        detail.setCreateTime(new Date());
        detail.setPath(request.getRequestURI());

        apiError.setException(detail);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), apiError);
    }
}
