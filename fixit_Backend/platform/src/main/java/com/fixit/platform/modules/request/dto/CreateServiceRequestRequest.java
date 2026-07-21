package com.fixit.platform.modules.request.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateServiceRequestRequest {

    @NotNull(message = "Gig ID is required")
    private UUID gigId;

    @NotBlank(message = "Job description is required")
    @Size(max = 3000, message = "Description cannot exceed 3000 characters")
    private String jobDescription;

    @NotBlank(message = "Service location is required")
    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String serviceLocation;

    @NotNull(message = "Scheduled date is required")
    @FutureOrPresent(message = "Scheduled date cannot be in the past")
    private LocalDate scheduledDate;

    @NotNull(message = "Proposed price is required")
    @DecimalMin(value = "0.01", message = "Proposed price must be greater than zero")
    private BigDecimal proposedPrice;
}