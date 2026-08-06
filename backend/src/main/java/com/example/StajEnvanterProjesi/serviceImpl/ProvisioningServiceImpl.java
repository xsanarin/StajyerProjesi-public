package com.example.StajEnvanterProjesi.serviceImpl;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.entity.Departman;
import com.example.StajEnvanterProjesi.entity.Kullanici;
import com.example.StajEnvanterProjesi.entity.Sorumlu;
import com.example.StajEnvanterProjesi.entity.Stajyer;
import com.example.StajEnvanterProjesi.entity.dto.Auth.ProvisionResponse;
import com.example.StajEnvanterProjesi.entity.dto.Auth.SorumluProvisionRequest;
import com.example.StajEnvanterProjesi.entity.dto.Auth.StajyerProvisionRequest;
import com.example.StajEnvanterProjesi.entity.enums.Rol;
import com.example.StajEnvanterProjesi.exception.BaseException;
import com.example.StajEnvanterProjesi.repository.DepartmanRepository;
import com.example.StajEnvanterProjesi.repository.KullaniciRepository;
import com.example.StajEnvanterProjesi.repository.SorumluRepository;
import com.example.StajEnvanterProjesi.repository.StajyerRepository;
import com.example.StajEnvanterProjesi.security.CurrentUser;
import com.example.StajEnvanterProjesi.service.ProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * ADIM 2 — Provisioning. Profil (Sorumlu/Stajyer) + Kullanici tek @Transactional metotta olusur:
 * biri basarisiz olursa hicbiri yazilmaz. Gecici sifre uretilir, BCrypt'le saklanir,
 * duz hali yalnizca response'ta bir kez doner.
 */
@Service
public class ProvisioningServiceImpl implements ProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(ProvisioningServiceImpl.class);

    // Gecici sifre tahmin edilebilir bir veriden (telefon vb.) turetilmez; tamamen rastgele.
    // Okunabilir alfabe: 0/O, 1/l gibi kafa karistiranlar disarida.
    private static final char[] SIFRE_ALFABE = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789".toCharArray();
    private static final int SIFRE_UZUNLUK = 12;
    private final SecureRandom random = new SecureRandom();

    private final SorumluRepository sorumluRepository;
    private final StajyerRepository stajyerRepository;
    private final KullaniciRepository kullaniciRepository;
    private final DepartmanRepository departmanRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageHelper messageHelper;
    private final CurrentUser currentUser;

    public ProvisioningServiceImpl(SorumluRepository sorumluRepository,
                                   StajyerRepository stajyerRepository,
                                   KullaniciRepository kullaniciRepository,
                                   DepartmanRepository departmanRepository,
                                   PasswordEncoder passwordEncoder,
                                   MessageHelper messageHelper,
                                   CurrentUser currentUser) {
        this.sorumluRepository = sorumluRepository;
        this.stajyerRepository = stajyerRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.departmanRepository = departmanRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageHelper = messageHelper;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional
    public ProvisionResponse sorumluOlustur(SorumluProvisionRequest request) {
        emailTekilMi(request.getEmail());

        Sorumlu sorumlu = new Sorumlu();
        sorumlu.setAd(request.getAd());
        sorumlu.setSoyad(request.getSoyad());
        sorumlu.setEmail(request.getEmail());
        sorumlu.setTelefon(request.getTelefon());
        if (request.getDepartmanHashId() != null && !request.getDepartmanHashId().isBlank()) {
            Departman departman = departmanRepository.findByHashId(request.getDepartmanHashId())
                    .orElseThrow(() -> new BaseException(messageHelper.get("error.departman.bulunamadi")));
            sorumlu.setDepartman(departman);
        }
        sorumlu.setOlusturanKullaniciId(currentUser.kullaniciIdOrNull());
        sorumlu = sorumluRepository.save(sorumlu);
        log.info("Sorumlu provision edildi: hedef={} olusturan={}", sorumlu.getHashId(), currentUser.aktorEtiketi());

        String geciciSifre = rastgeleSifreUret();
        Kullanici kullanici = new Kullanici();
        kullanici.setEmail(request.getEmail());
        kullanici.setSifreHash(passwordEncoder.encode(geciciSifre));
        kullanici.setRol(Rol.SORUMLU);
        kullanici.setSifreDegistirmeli(true);
        kullanici.setSorumlu(sorumlu);
        kullaniciRepository.save(kullanici);

        return response(sorumlu.getHashId(), request.getEmail(), Rol.SORUMLU, geciciSifre);
    }

    @Override
    @Transactional
    public ProvisionResponse stajyerOlustur(StajyerProvisionRequest request) {
        emailTekilMi(request.getEmail());

        Stajyer stajyer = new Stajyer();
        stajyer.setAd(request.getAd());
        stajyer.setSoyad(request.getSoyad());
        stajyer.setEmail(request.getEmail());
        stajyer.setTelefon(request.getTelefon());
        stajyer.setUniversite(request.getUniversite());
        stajyer.setBolum(request.getBolum());
        stajyer.setSinif(request.getSinif());
        stajyer.setOlusturanKullaniciId(currentUser.kullaniciIdOrNull());
        stajyer = stajyerRepository.save(stajyer);
        log.info("Stajyer provision edildi: hedef={} olusturan={}", stajyer.getHashId(), currentUser.aktorEtiketi());

        String geciciSifre = rastgeleSifreUret();
        Kullanici kullanici = new Kullanici();
        kullanici.setEmail(request.getEmail());
        kullanici.setSifreHash(passwordEncoder.encode(geciciSifre));
        kullanici.setRol(Rol.STAJYER);
        kullanici.setSifreDegistirmeli(true);
        kullanici.setStajyer(stajyer);
        kullaniciRepository.save(kullanici);

        return response(stajyer.getHashId(), request.getEmail(), Rol.STAJYER, geciciSifre);
    }

    // ---- yardimcilar ----

    private void emailTekilMi(String email) {
        if (kullaniciRepository.existsByEmail(email)) {
            throw new BaseException(messageHelper.get("error.kullanici.mail.mevcut"));
        }
    }

    private String rastgeleSifreUret() {
        StringBuilder sb = new StringBuilder(SIFRE_UZUNLUK);
        for (int i = 0; i < SIFRE_UZUNLUK; i++) {
            sb.append(SIFRE_ALFABE[random.nextInt(SIFRE_ALFABE.length)]);
        }
        return sb.toString();
    }

    private ProvisionResponse response(String profilHashId, String email, Rol rol, String geciciSifre) {
        ProvisionResponse r = new ProvisionResponse();
        r.setProfilHashId(profilHashId);
        r.setEmail(email);
        r.setRol(rol);
        r.setGeciciSifre(geciciSifre);
        r.setMesaj("Gecici sifre yalnizca bir kez gosterilir. Kullanici ilk giriste degistirecek.");
        return r;
    }
}
