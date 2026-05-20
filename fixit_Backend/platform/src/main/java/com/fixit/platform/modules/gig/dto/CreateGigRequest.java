package com.fixit.platform.modules.gig.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateGigRequest {

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(max = 3000)
    private String description;

    @NotNull
    private UUID skillId;

    @DecimalMin("0.0")
    private BigDecimal price;

}
