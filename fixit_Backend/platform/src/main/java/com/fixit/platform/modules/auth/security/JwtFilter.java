package com.fixit.platform.modules.auth.security;


import com.fixit.platform.modules.auth.service.AuthCookieService;
import com.fixit.platform.modules.auth.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthCookieService authCookieService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Find JWT from header or cookie
        String token = resolveToken(request);

        // 2. Validate JWT
        if (token != null && jwtService.isTokenValid(token)) {

            // 3. Extract information from JWT
            UUID userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            List<String> roles = jwtService.extractRoles(token);

            // 4. Convert roles into Spring Security authorities
            List<SimpleGrantedAuthority> authorities =
                    roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            // 5. Create our authenticated user representation
            CustomUserDetails userDetails =
                    new CustomUserDetails(
                            userId,
                            email,
                            authorities
                    );

            // 6. Create Spring Security Authentication
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // 7. Store authentication for this request
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authToken);
        }

        // 8. Continue the request
        filterChain.doFilter(request, response);
    }

    /**
     * Resolves the JWT from either:
     * 1. Authorization: Bearer <token>
     * 2. fixit_access_token cookie
     */
    private String resolveToken(HttpServletRequest request) {

        // Approach 1: Authorization header
        String authHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null &&
                authHeader.startsWith("Bearer ")) {

            return authHeader.substring(7);
        }

        // Approach 2: HttpOnly cookie
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie ->
                        authCookieService
                                .getCookieName()
                                .equals(cookie.getName())
                )
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}