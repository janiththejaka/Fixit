package com.fixit.platform.modules.quotation.dto;

import com.fixit.platform.modules.quotation.entity.QuotationStatus;
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
@AllArgsConstructor
@NoArgsConstructor
public class QuotationResponse {

    private UUID id;

    private UUID providerProfileId;
    private UUID customerProfileId;

    private String providerName;
    private String customerName;

    private UUID skillId;
    private String skillName;

    private String jobDescription;
    private String serviceLocation;
    private LocalDate scheduledDate;
    private String customerNote;

    private BigDecimal quotedPrice;
    private String providerMessage;

    private QuotationStatus status;

    private UUID serviceRequestId;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
