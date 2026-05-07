package com.fixit.platform.modules.auth.service;

import com.fixit.platform.modules.auth.dto.ClientRegisterRequest;
import com.fixit.platform.modules.auth.dto.LoginRequest;
import com.fixit.platform.modules.auth.dto.ProviderRegisterRequest;
import com.fixit.platform.modules.auth.dto.RegisterRequest;
import com.fixit.platform.modules.auth.entity.AuthRole;
import com.fixit.platform.modules.auth.entity.AuthUserRole;
import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.AuthRoleRepository;
import com.fixit.platform.modules.auth.repository.AuthUserRoleRepository;
import com.fixit.platform.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthRoleRepository roleRepository;
    private final AuthUserRoleRepository userRoleRepository;

//    public void register(RegisterRequest request) {
//
//        // Check if email already exists
//        userRepository.findByEmail(request.getEmail())
//                .ifPresent(user -> {
//                    throw new RuntimeException("Email already exists");
//                });
//
//        // Create user
//        User user = new User();
//        user.setEmail(request.getEmail());
//
//        //  Hash password
//        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
//
//        userRepository.save(user);
//    }


    public void registerClient(ClientRegisterRequest request) {

        // Check if email already exists
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists");
                });

        User user = new User();
        user.setEmail(request.email);
        user.setPasswordHash(passwordEncoder.encode(request.password));

        userRepository.save(user);

        AuthRole role = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow();

        AuthUserRole userRole = new AuthUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());

        userRoleRepository.save(userRole);
    }

    public void registerProvider(ProviderRegisterRequest request) {

        User user = new User();
        user.setEmail(request.email);
        user.setPasswordHash(passwordEncoder.encode(request.password));

        userRepository.save(user);

        AuthRole role = roleRepository.findByName("ROLE_PROVIDER")
                .orElseThrow();

        AuthUserRole userRole = new AuthUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());

        userRoleRepository.save(userRole);
    }






    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        List<AuthUserRole> userRoles = userRoleRepository.findByUserId(user.getId());

        List<String> roles = userRoles.stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()).orElseThrow().getName())
                .toList();

        return jwtService.generateToken(user.getEmail(), roles);
    }
}
