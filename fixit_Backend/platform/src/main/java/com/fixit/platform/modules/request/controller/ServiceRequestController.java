package com.fixit.platform.modules.request.controller;


import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.request.dto.CreateServiceRequestRequest;
import com.fixit.platform.modules.request.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final UserRepository userRepository;

    public ServiceRequestController(
            ServiceRequestService serviceRequestService,
            UserRepository userRepository
    ) {
        this.serviceRequestService = serviceRequestService;
        this.userRepository = userRepository;
    }

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
}
