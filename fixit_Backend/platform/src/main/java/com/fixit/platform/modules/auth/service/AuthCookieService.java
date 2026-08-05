package com.fixit.platform.modules.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {

    @Value("${app.auth.cookie-name}")
    private String cookieName;

    @Value("${app.auth.cookie-max-age}")
    private long maxAge;

    @Value("${app.auth.cookie-secure}")
    private boolean secure;

    @Value("${app.auth.cookie-same-site}")
    private String sameSite;

    public ResponseCookie createAccessTokenCookie(String token) {

        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .build();
    }

    public ResponseCookie clearAccessTokenCookie() {

        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    public String getCookieName() {
        return cookieName;
    }
}
