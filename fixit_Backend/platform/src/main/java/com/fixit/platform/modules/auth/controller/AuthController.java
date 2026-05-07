package com.fixit.platform.modules.auth.controller;

import com.fixit.platform.modules.auth.dto.ClientRegisterRequest;
import com.fixit.platform.modules.auth.dto.LoginRequest;
import com.fixit.platform.modules.auth.dto.ProviderRegisterRequest;
import com.fixit.platform.modules.auth.dto.RegisterRequest;
import com.fixit.platform.modules.auth.service.AuthService;
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
    public ResponseEntity<?> registerClient(@RequestBody ClientRegisterRequest request) {
        authService.registerClient(request);
        return ResponseEntity.ok("Client registered");
    }
    @PostMapping("/register/provider")
    public ResponseEntity<?> registerProvider(@RequestBody ProviderRegisterRequest request) {
        authService.registerProvider(request);
        return ResponseEntity.ok("Provider registered");
    }


    @GetMapping("/provider-only")
    @PreAuthorize("hasRole('PROVIDER')")
    public String providerOnly() {
        return "Only providers can see this";
    }

    @GetMapping("/client-only")
    @PreAuthorize("hasRole('CLIENT')")
    public String clientOnly() {
        return "Only client can see this";
    }





    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        String token = authService.login(request);

        return ResponseEntity.ok(token);
    }
}
