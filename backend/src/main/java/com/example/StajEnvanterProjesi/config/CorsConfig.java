package com.example.StajEnvanterProjesi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * FRONTEND ADIMI — Tarayici erisimi.
 *
 * Backend stateless JWT ile calisiyor (cookie yok, kimlik Authorization header'inda).
 * Tarayicidaki React uygulamasi farkli bir origin'de (Vite dev sunucusu :5173) kostugu
 * icin, CORS tanimlanmadan preflight (OPTIONS) istekleri reddedilir ve hicbir cagri
 * tarayicidan calismaz. Postman'de calisip tarayicidan calismamasinin sebebi budur.
 *
 * Izin verilen origin'ler app.cors.origins ile override edilebilir (prod'da env ver).
 * allowCredentials KAPALI: token cookie'de degil header'da tasindigi icin gerekmiyor,
 * kapali birakmak "*" benzeri gevsemelerin guvenlik etkisini de sinirlar.
 */
@Configuration
public class CorsConfig {

    private final List<String> izinliOriginler;

    public CorsConfig(@Value("${app.cors.origins:http://localhost:5173,http://127.0.0.1:5173}")
                      List<String> izinliOriginler) {
        this.izinliOriginler = izinliOriginler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(izinliOriginler);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        // Preflight sonucu 1 saat cache'lensin; her istekte OPTIONS turu atilmasin.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}