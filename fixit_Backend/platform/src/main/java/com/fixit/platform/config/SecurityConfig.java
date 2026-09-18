package com.fixit.platform.config;

import com.fixit.platform.modules.auth.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        // Plain handler: cookie value == JSON body token == X-XSRF-TOKEN header value.
                        // SpaCsrfTokenRequestHandler XOR-masks the JSON body token, causing a
                        // mismatch with the raw cookie value when tested via Postman or any
                        // REST client that isn't a browser reading the cookie directly.
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()
                        )
                        .ignoringRequestMatchers(
                                "/api/auth/login",
                                "/api/auth/register/**"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Only public auth endpoints (login & register) are permit-all
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/skills/**",
                                "/api/profile/providers",
                                "/api/profile/providers/{profileId}",

                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"

                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/gigs/**"
                        ).permitAll()
                        // Everything else requires authentication;
                        // role checks are handled by @PreAuthorize on individual methods
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        // 401 - request has no valid JWT at all (not authenticated)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write(
                                    "{\"error\": \"Unauthorized\", \"message\": \"Valid authentication token required\"}"
                            );
                        })
                        // 403 - authenticated but wrong role
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write(
                                    "{\"error\": \"Forbidden\", \"message\": \"You do not have permission to access this resource\"}"
                            );
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


