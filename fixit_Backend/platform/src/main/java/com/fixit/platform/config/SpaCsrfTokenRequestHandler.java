package com.fixit.platform.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public final class SpaCsrfTokenRequestHandler
        implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain =
            new CsrfTokenRequestAttributeHandler();

    private final CsrfTokenRequestHandler xor =
            new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Supplier<CsrfToken> csrfToken
    ) {
        /*
         * Use XOR masking when exposing the token
         * as a request attribute.
         */
        this.xor.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(
            HttpServletRequest request,
            CsrfToken csrfToken
    ) {
        String headerValue =
                request.getHeader(csrfToken.getHeaderName());

        /*
         * If the token comes from the X-XSRF-TOKEN header,
         * use the plain token from the cookie.
         *
         * If it comes from a request parameter, use
         * the XOR handler.
         */
        return StringUtils.hasText(headerValue)
                ? this.plain.resolveCsrfTokenValue(
                request,
                csrfToken
        )
                : this.xor.resolveCsrfTokenValue(
                request,
                csrfToken
        );
    }
}
