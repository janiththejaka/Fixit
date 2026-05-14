package com.fixit.platform.modules.profile.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Getter
@Setter
@Service
public class ProfileResponse {

    private UUID id;
    private String fullName;
    private String phoneNumber;
    private String location;
    private String bio;
    private String profileImageUrl;
    private boolean providerProfileComplete;


}
