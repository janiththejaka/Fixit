package com.fixit.platform.modules.profile.controller;

import com.fixit.platform.modules.profile.dto.SkillResponse;
import com.fixit.platform.modules.profile.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final ProfileService profileService;

    public SkillController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public List<SkillResponse> getSkills() {
        return profileService.getAllSkills();
    }
}
