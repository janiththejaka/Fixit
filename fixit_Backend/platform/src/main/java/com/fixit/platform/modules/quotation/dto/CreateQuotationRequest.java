package com.fixit.platform.modules.quotation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateQuotationRequest {

    @NotNull(message = "Provider profile is required")
    private UUID providerProfileId;

    @NotNull(message = "Skill is required")
    private UUID skillId;

    @NotBlank(message = "Job description is required")
    @Size(max = 3000)
    private String jobDescription;

    @Size(max = 200)
    private String serviceLocation;

    @FutureOrPresent
    private LocalDate scheduledDate;

    @Size(max = 1000)
    private String customerNote;
}