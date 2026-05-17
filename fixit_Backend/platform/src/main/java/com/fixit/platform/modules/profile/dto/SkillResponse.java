package com.fixit.platform.modules.profile.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SkillResponse {

    private UUID id;
    private String name;
    private String slug;

}
