package com.fixit.platform.modules.gig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class UpdateGigRequest {


    private String title;

    private String description;

    private BigDecimal price;

    private UUID skillId;

    private String imageUrl;



}
