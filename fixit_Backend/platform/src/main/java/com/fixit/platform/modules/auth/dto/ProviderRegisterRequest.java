package com.fixit.platform.modules.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProviderRegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    public String email;

    @Size(min = 4, message = "Password must be at least 6 characters")
    public String password;

    @NotNull(message = "Primary skill required")
    public UUID primarySkillId;
}
