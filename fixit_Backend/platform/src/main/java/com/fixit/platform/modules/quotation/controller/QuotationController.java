package com.fixit.platform.modules.quotation.controller;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.quotation.dto.CreateQuotationRequest;
import com.fixit.platform.modules.quotation.dto.QuotationResponse;
import com.fixit.platform.modules.quotation.dto.RespondQuotationRequest;
import com.fixit.platform.modules.quotation.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;
    private final UserRepository userRepository;


    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<String> createQuotation(
            Authentication authentication,
            @Valid @RequestBody
            CreateQuotationRequest request
    ) {
        User customer = getAuthenticatedUser(authentication);

        return quotationService.createQuotation(customer.getId(), request);
    }


    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public List<QuotationResponse> getMyQuotations(Authentication authentication)
    {
        User customer = getAuthenticatedUser(authentication);

        return quotationService.getCustomerQuotations(customer.getId());
    }


    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER')")
    public List<QuotationResponse>
    getProviderQuotations(Authentication authentication)
    {
        User provider = getAuthenticatedUser(authentication);

        return quotationService.getProviderQuotations(provider.getId());
    }
    /*
     * Provider sends price and message.
     */
    @PatchMapping("/{quotationId}/respond")
    @PreAuthorize("hasRole('PROVIDER')")
    public ApiResponse<String> respondQuotation(
            Authentication authentication,
            @PathVariable UUID quotationId,
            @Valid @RequestBody
            RespondQuotationRequest request
    ) {
        User provider = getAuthenticatedUser(
                authentication
        );

        return quotationService.respondQuotation(
                provider.getId(),
                quotationId,
                request
        );
    }

    @PatchMapping("/{quotationId}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<String> acceptQuotation(
            Authentication authentication,
            @PathVariable UUID quotationId
    ) {
        User customer = getAuthenticatedUser(
                authentication
        );

        return quotationService.acceptQuotation(
                customer.getId(),
                quotationId
        );
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {
        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }
}
