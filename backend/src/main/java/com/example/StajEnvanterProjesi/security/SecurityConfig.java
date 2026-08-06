package com.example.StajEnvanterProjesi.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * ADIM 2 — Stateless JWT guvenligi.
 * Rol bazli erisim: SORUMLU token'i /api/admin/**'e 403 alir.
 *
 * GUVENLIK DUZELTMESI: Auth eklenmeden once yazilmis eski uc noktalar (/staj, /sorumlu,
 * /donem, /departman, /stajyer, /stajyerApplication) yalnizca anyRequest().authenticated()
 * kapsamindaydi — yani GECERLI HERHANGI BIR TOKEN yetiyordu, rol sorulmuyordu. Bir STAJYER
 * kendi token'iyla departman/staj CRUD yapabiliyor, tum kisi listesini cekebiliyordu
 * (yetki yukseltme). Bu uc noktalar admin-yonetim islemleri oldugu icin artik acikca
 * ADMIN'e kilitli.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          CorsConfigurationSource corsConfigurationSource,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // FRONTEND ADIMI: CorsConfig'teki kaynak devreye alinir. Bu satir olmadan
                // bean tanimli olsa bile Spring Security zincirinde CORS uygulanmaz ve
                // tarayicidan gelen preflight (OPTIONS) istekleri 401/403 ile reddedilir.
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Kimliksiz istek -> 401, kimlik var ama rol yetmiyor -> 403.
                // Bu ikisi ayrilmadan once ikisi de 403 donuyordu (formLogin/httpBasic kapali
                // oldugu icin varsayilan Http403ForbiddenEntryPoint devredeydi) ve frontend
                // suresi dolmus token'i "rolun yetmiyor" sanip kullaniciyi login'e atmiyordu.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // ERROR dispatch'i yetkilendirmeden muaf. sendError cagiran herhangi
                        // bir yol (ornegin Spring'in kendi hata sayfasi) konteynerde /error'a
                        // ikinci bir dispatch acar; guvenlik zinciri orada bastan calisir ve
                        // SecurityContext bos oldugu icin istek anonim gorunur. Muaf tutulmazsa
                        // asil durum kodunun (403/500) uzerine 401 yazilir.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/sorumlu/**").hasRole("SORUMLU")
                        .requestMatchers("/api/me/**").hasRole("STAJYER")
                        // Eski cıplak uc noktalar = admin-yonetim (departman/donem/kisi/staj
                        // CRUD). Onceden her token'i kabul ediyorlardi; artik ADMIN'e kilitli.
                        // Not: /api/** ile baslamadiklari icin yukaridaki kurallari etkilemez.
                        .requestMatchers(
                                "/departman/**", "/donem/**", "/sorumlu/**",
                                "/stajyer/**", "/staj/**", "/stajyerApplication/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
