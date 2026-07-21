package com.fixit.platform.modules.request.service;

import com.fixit.platform.common.exception.UserNotFoundException;
import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.gig.entity.Gig;
import com.fixit.platform.modules.gig.repository.GigRepository;
import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.repository.ProfileRepository;
import com.fixit.platform.modules.request.dto.CreateServiceRequestRequest;
import com.fixit.platform.modules.request.entity.ServiceRequest;
import com.fixit.platform.modules.request.entity.ServiceRequestStatus;
import com.fixit.platform.modules.request.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ProfileRepository profileRepository;
    private final GigRepository gigRepository;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository,
            ProfileRepository profileRepository,
            GigRepository gigRepository
    ) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.profileRepository = profileRepository;
        this.gigRepository = gigRepository;
    }

    @Transactional
    public ApiResponse<String> createServiceRequest(
            UUID customerUserId,
            CreateServiceRequestRequest request
    ) {
        // Auth user -> customer profile
        Profile customerProfile = profileRepository
                .findByUserId(customerUserId)
                .orElseThrow(() ->
                        new UserNotFoundException("Customer profile not found")
                );

        // Requested gig must exist
        Gig gig = gigRepository
                .findById(request.getGigId())
                .orElseThrow(() ->
                        new RuntimeException("Gig not found")
                );

        // Inactive gigs cannot receive new requests
        if (!gig.isActive()) {
            throw new RuntimeException(
                    "This gig is currently unavailable"
            );
        }

        // Provider identity is derived from the gig
        Profile providerProfile = profileRepository
                .findById(gig.getProfileId())
                .orElseThrow(() ->
                        new RuntimeException("Provider profile not found")
                );

        if (!providerProfile.isProviderProfileComplete()) {
            throw new RuntimeException(
                    "Provider is currently unavailable"
            );
        }

        // A user must not request their own service
        if (customerProfile.getId().equals(providerProfile.getId())) {
            throw new RuntimeException(
                    "You cannot request your own gig"
            );
        }

        // Prevent duplicate active requests
        boolean duplicateRequest =
                serviceRequestRepository
                        .existsByCustomerProfileIdAndGigIdAndStatusIn(
                                customerProfile.getId(),
                                gig.getId(),
                                List.of(
                                        ServiceRequestStatus.PENDING,
                                        ServiceRequestStatus.PROVIDER_ACCEPTED,
                                        ServiceRequestStatus.IN_PROGRESS,
                                        ServiceRequestStatus.COMPLETED_BY_PROVIDER
                                )
                        );

        if (duplicateRequest) {
            throw new RuntimeException(
                    "You already have an active request for this gig"
            );
        }

        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setCustomerProfileId(customerProfile.getId());
        serviceRequest.setProviderProfileId(providerProfile.getId());
        serviceRequest.setGigId(gig.getId());

        serviceRequest.setJobDescription(request.getJobDescription());
        serviceRequest.setServiceLocation(request.getServiceLocation());
        serviceRequest.setScheduledDate(request.getScheduledDate());
        serviceRequest.setProposedPrice(request.getProposedPrice());

        serviceRequest.setAgreedPrice(null);
        serviceRequest.setStatus(ServiceRequestStatus.PENDING);

        serviceRequestRepository.save(serviceRequest);

        return new ApiResponse<>(
                true,
                "Service request created successfully",
                serviceRequest.getId().toString()
        );
    }
}
