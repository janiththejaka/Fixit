package com.fixit.platform.modules.request.controller;


import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.request.dto.CreateServiceRequestRequest;
import com.fixit.platform.modules.request.dto.ServiceRequestResponse;
import com.fixit.platform.modules.request.dto.UpdateRequestStatusRequest;
import com.fixit.platform.modules.request.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final UserRepository userRepository;


    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<String> createServiceRequest(
            Authentication authentication,
            @Valid @RequestBody CreateServiceRequestRequest request
    ) {
        String email = authentication.getName();

        User customer = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return serviceRequestService.createServiceRequest(
                customer.getId(),
                request
        );
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public List<ServiceRequestResponse> getMyRequests(
            Authentication authentication
    ) {
        String email = authentication.getName();

        User customer = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return serviceRequestService.getCustomerRequests(
                customer.getId()
        );
    }

    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER')")
    public List<ServiceRequestResponse> getProviderRequests(
            Authentication authentication
    ) {
        String email = authentication.getName();

        User provider = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return serviceRequestService.getProviderRequests(
                provider.getId()
        );
    }

    //Actio status change endpoint
    @PatchMapping("/{requestId}/status")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public ApiResponse<String> updateRequestStatus(
            Authentication authentication,
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateRequestStatusRequest request
    ) {
        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return serviceRequestService.updateRequestStatus(
                user.getId(),
                requestId,
                request.getAction()
        );
    }
}
