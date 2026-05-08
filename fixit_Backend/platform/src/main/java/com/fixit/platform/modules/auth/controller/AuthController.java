package com.fixit.platform.modules.auth.controller;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.dto.ClientRegisterRequest;
import com.fixit.platform.modules.auth.dto.LoginRequest;
import com.fixit.platform.modules.auth.dto.ProviderRegisterRequest;
import com.fixit.platform.modules.auth.dto.RegisterRequest;
import com.fixit.platform.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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


    @GetMapping("/provider-only")
    @PreAuthorize("hasRole('PROVIDER')")
    public String providerOnly() {
        return "Only providers can see this";
    }







    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {

        ApiResponse<String> response = authService.login(request);

        return ResponseEntity.ok(response);    }
}
