package com.fixit.platform.modules.auth.controller;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.dto.ClientRegisterRequest;
import com.fixit.platform.modules.auth.dto.LoginRequest;
import com.fixit.platform.modules.auth.dto.ProviderRegisterRequest;
import com.fixit.platform.modules.auth.service.AuthCookieService;
import com.fixit.platform.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Authentication",
        description = "Client/provider registration and login"
)

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService  authCookieService;

//    @PostMapping("/register")
//    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
//
//        authService.register(request);
//
//        return ResponseEntity.ok("User registered successfully");
//    }

    @PostMapping("/register/client")
    public ResponseEntity<?> registerClient(@Valid @RequestBody ClientRegisterRequest request) {
        ApiResponse<String> response = authService.registerClient(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register/provider")
    public ResponseEntity<?> registerProvider(@Valid @RequestBody ProviderRegisterRequest request) {
        ApiResponse<String> response = authService.registerProvider(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        String token = authService.login(request);

        ResponseCookie cookie =
                authCookieService.createAccessTokenCookie(token);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        new ApiResponse<>(
                                true,
                                "Login successful",
                                null
                        )
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {

        ResponseCookie cookie = authCookieService.clearAccessTokenCookie();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        new ApiResponse<>(
                                true,
                                "Logout successful",
                                null
                        )
                );
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
