package com.fixit.platform.modules.gig.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class GigCardResponse {

    private UUID gigId;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String providerName;
    private String providerLocation;
    private String skillName;

}
