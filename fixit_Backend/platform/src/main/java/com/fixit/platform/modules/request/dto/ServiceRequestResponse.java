package com.fixit.platform.modules.request.dto;

import com.fixit.platform.modules.request.entity.ServiceRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponse {

    private UUID requestId;

    private UUID gigId;
    private String gigTitle;

    private UUID customerProfileId;
    private String customerName;

    private UUID providerProfileId;
    private String providerName;

    private String jobDescription;
    private String serviceLocation;
    private LocalDate scheduledDate;

    private BigDecimal proposedPrice;
    private BigDecimal agreedPrice;

    private ServiceRequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
