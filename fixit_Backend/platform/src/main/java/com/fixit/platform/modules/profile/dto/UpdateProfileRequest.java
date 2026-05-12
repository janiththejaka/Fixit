package com.fixit.platform.modules.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 150)
    private String fullName;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 150)
    private String location;

    @Size(max = 1000)
    private String bio;

    private String profileImageUrl;

}
