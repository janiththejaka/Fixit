package com.fixit.platform.modules.auth.service;

import com.fixit.platform.common.exception.EmailAlreadyExistsException;
import com.fixit.platform.common.exception.InvalidCredentialsException;
import com.fixit.platform.common.exception.ResourceNotFoundException;
import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.dto.ClientRegisterRequest;
import com.fixit.platform.modules.auth.dto.LoginRequest;
import com.fixit.platform.modules.auth.dto.ProviderRegisterRequest;
import com.fixit.platform.modules.auth.entity.AuthRole;
import com.fixit.platform.modules.auth.entity.AuthUserRole;
import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.AuthRoleRepository;
import com.fixit.platform.modules.auth.repository.AuthUserRoleRepository;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.entity.ProviderSkill;
import com.fixit.platform.modules.profile.entity.Skill;
import com.fixit.platform.modules.profile.repository.ProviderSkillRepository;
import com.fixit.platform.modules.profile.repository.SkillRepository;
import com.fixit.platform.modules.profile.service.ProfileService;
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
    private final ProfileService profileService;
    private final SkillRepository skillRepository;
    private final ProviderSkillRepository providerSkillRepository;

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


    public ApiResponse<String> registerClient(ClientRegisterRequest request) {

        // Check if email already exists
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new EmailAlreadyExistsException("Email already exists");
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

        profileService.createBasicProfile(
                user.getId(),
                request.getFullName()
        );

        return new ApiResponse<>(
                true,
                "Client registered successfully",
                null
        );
    }

    public ApiResponse<String> registerProvider(ProviderRegisterRequest request) {

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

        Profile profile =
                profileService.createBasicProfile(
                        user.getId(),
                        request.getFullName()
                );

        Skill skill = skillRepository.findById(
                request.getPrimarySkillId()
        ).orElseThrow(
                () -> new ResourceNotFoundException("Skill Not Found")
        );

        ProviderSkill providerSkill =
                new ProviderSkill();

        providerSkill.setProfileId(
                profile.getId()
        );

        providerSkill.setSkill(skill);

        providerSkillRepository.save(
                providerSkill
        );

        return new ApiResponse<>(
                true,
                "Provider registered successfully",
                null
        );
    }






    public ApiResponse<String> login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        List<AuthUserRole> userRoles = userRoleRepository.findByUserId(user.getId());

        List<String> roles = userRoles.stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()).orElseThrow().getName())
                .toList();

        String token = jwtService.generateToken(user.getEmail(), roles);

        return new ApiResponse<>(
                true,
                "Login successful",
                token
        );
    }
}
