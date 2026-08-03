package com.fixit.platform.modules.quotation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RespondQuotationRequest {

    @NotNull(message = "Quoted price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal quotedPrice;

    @Size(max = 1000)
    private String providerMessage;
}
