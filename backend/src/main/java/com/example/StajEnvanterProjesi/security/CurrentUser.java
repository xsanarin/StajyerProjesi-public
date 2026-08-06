package com.example.StajEnvanterProjesi.security;

import com.example.StajEnvanterProjesi.MessageHelper;
import com.example.StajEnvanterProjesi.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * ADIM 2 — SecurityContext'ten giris yapan kullaniciyi okuyan yardimci.
 * Controller/Service, kimligi query param yerine buradan alir (B'ye teslim edilen sozlesme):
 *   Long sorumluId = currentUser.sorumluId();
 */
@Component
public class CurrentUser {

    @Autowired MessageHelper messageHelper;

    public AuthPrincipal get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new BaseException(messageHelper.get("error.kimlik.dogrulanmamis"));
        }
        return principal;
    }

    public Long kullaniciId() {
        return get().kullaniciId();
    }

    /** rol=SORUMLU olmayan token icin null olabilir. */
    public Long sorumluId() {
        return get().sorumluId();
    }

    /** rol=STAJYER olmayan token icin null olabilir. */
    public Long stajyerId() {
        return get().stajyerId();
    }

    // ---- Audit/log yardimcilari ----
    // get() kimlik yoksa exception atar; audit alanlari HTTP disi akislarda (seeder,
    // zamanlanmis is) da doldurulabilmeli, o yuzden bu metotlar patlamaz, null/SISTEM doner.

    private AuthPrincipal getOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof AuthPrincipal p) ? p : null;
    }

    /** Giris yapan kullanicinin id'si; kimlik yoksa null. Audit kolonlarina yazilir. */
    public Long kullaniciIdOrNull() {
        AuthPrincipal p = getOrNull();
        return p != null ? p.kullaniciId() : null;
    }

    /** Log satirlari icin okunabilir aktor etiketi; kimlik yoksa "SISTEM". */
    public String aktorEtiketi() {
        AuthPrincipal p = getOrNull();
        return p != null ? "kullaniciId=" + p.kullaniciId() + " <" + p.email() + ">" : "SISTEM";
    }
}
