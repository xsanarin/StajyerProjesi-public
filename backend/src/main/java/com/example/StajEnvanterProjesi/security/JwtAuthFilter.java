package com.example.StajEnvanterProjesi.security;

import com.example.StajEnvanterProjesi.entity.Kullanici;
import com.example.StajEnvanterProjesi.entity.enums.Rol;
import com.example.StajEnvanterProjesi.repository.KullaniciRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * ADIM 2 — Her istekte Authorization: Bearer <token> basligini cozer.
 * KRITIK: token gecerli olsa bile kullanici.status DB'den kontrol edilir; boylece
 * kilitlenen hesap (ADIM 3 sorumlu ayrilma) token suresi dolmadan da aninda dusen olur.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final KullaniciRepository kullaniciRepository;

    public JwtAuthFilter(JwtService jwtService, KullaniciRepository kullaniciRepository) {
        this.jwtService = jwtService;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = jwtService.parse(token);
            Long kullaniciId = Long.valueOf(claims.getSubject());

            // Her istekte DB kontrolu: hesap hala aktif mi?
            Optional<Kullanici> aktif = kullaniciRepository.findByIdAndStatusTrue(kullaniciId);
            if (aktif.isPresent()) {
                Kullanici k = aktif.get();
                Rol rol = k.getRol();
                AuthPrincipal principal = new AuthPrincipal(
                        k.getId(),
                        k.getEmail(),
                        rol,
                        k.getSorumlu() != null ? k.getSorumlu().getId() : null,
                        k.getStajyer() != null ? k.getStajyer().getId() : null
                );
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Gecersiz/expired token -> kimlik konmaz, istek anonim devam eder (korumali uc 401/403 verir).
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
