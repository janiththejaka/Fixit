package com.fixit.platform.modules.profile.controller;

import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.profile.dto.AddProviderSkillRequest;
import com.fixit.platform.modules.profile.dto.ProviderSkillResponse;
import com.fixit.platform.modules.profile.dto.SkillResponse;
import com.fixit.platform.modules.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final ProfileService profileService;
    private final UserRepository userRepository;


    @GetMapping
    public List<SkillResponse> getSkills() {
        return profileService.getAllSkills();
    }

    // endpoints for add, view , delete skill by provider

    @PostMapping("/provider/skills")
    @PreAuthorize("hasRole('PROVIDER')")
    public String addProviderSkill(
            Authentication authentication,
            @RequestBody AddProviderSkillRequest request
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        profileService.addProviderSkill(
                user.getId(),
                request.getSkillId()
        );

        return "Skill added";
    }

    @GetMapping("/provider/skills")
    @PreAuthorize("hasRole('PROVIDER')")
    public List<ProviderSkillResponse>
    getProviderSkills(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return profileService.getProviderSkills(
                user.getId()
        );
    }

    @DeleteMapping("/provider/skills/{skillId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public String removeProviderSkill(
            Authentication authentication,
            @PathVariable UUID skillId
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        profileService.removeProviderSkill(
                user.getId(),
                skillId
        );

        return "Skill removed";
    }
}
