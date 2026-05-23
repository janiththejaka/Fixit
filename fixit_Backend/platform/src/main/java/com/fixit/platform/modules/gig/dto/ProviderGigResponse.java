package com.fixit.platform.modules.gig.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
public class ProviderGigResponse {

    private UUID id;

    private String title;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private boolean active;

    private String skillName;

    private LocalDateTime createdAt;

}
