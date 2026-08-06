package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Kullanici;
import com.example.StajEnvanterProjesi.entity.dto.Auth.LoginRequest;
import com.example.StajEnvanterProjesi.entity.dto.Auth.LoginResponse;
import com.example.StajEnvanterProjesi.entity.dto.Auth.SifreDegistirRequest;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.KullaniciRepository;
import com.example.StajEnvanterProjesi.security.JwtService;
import com.example.StajEnvanterProjesi.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * ADIM 2 — Login + ilk giriste zorunlu sifre degisimi.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MessageHelper messageHelper;

    public AuthServiceImpl(KullaniciRepository kullaniciRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           MessageHelper messageHelper) {
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.messageHelper = messageHelper;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // status=false (kilitli/ayrilmis) hesap zaten bulunamaz -> giris reddedilir.
        Kullanici k = kullaniciRepository.findByEmailAndStatusTrue(request.getEmail())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.auth.gecersiz")));

        if (!passwordEncoder.matches(request.getSifre(), k.getSifreHash())) {
            throw new BaseException(messageHelper.get("error.auth.gecersiz"));
        }

        // Ilk giris: tam token yerine "once sifre degistir" yaniti.
        if (Boolean.TRUE.equals(k.getSifreDegistirmeli())) {
            return LoginResponse.sifreDegistir(k.getRol(), messageHelper.get("error.auth.sifre.degistirmeli"));
        }

        return LoginResponse.token(jwtService.generate(k), k.getRol());
    }

    @Override
    public LoginResponse sifreDegistir(SifreDegistirRequest request) {
        Kullanici k = kullaniciRepository.findByEmailAndStatusTrue(request.getEmail())
                .orElseThrow(() -> new BaseException(messageHelper.get("error.auth.gecersiz")));

        if (!passwordEncoder.matches(request.getEskiSifre(), k.getSifreHash())) {
            throw new BaseException(messageHelper.get("error.auth.gecersiz"));
        }

        k.setSifreHash(passwordEncoder.encode(request.getYeniSifre()));
        k.setSifreDegistirmeli(false);
        kullaniciRepository.save(k);

        // Yeni sifreyle direkt tam token don -> istemci ikinci login yapmak zorunda kalmaz.
        return LoginResponse.token(jwtService.generate(k), k.getRol());
    }
}
