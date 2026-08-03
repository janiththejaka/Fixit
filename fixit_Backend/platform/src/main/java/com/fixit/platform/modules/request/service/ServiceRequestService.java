package com.fixit.platform.modules.request.service;

import com.fixit.platform.common.exception.UserNotFoundException;
import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.gig.entity.Gig;
import com.fixit.platform.modules.gig.repository.GigRepository;
import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.repository.ProfileRepository;
import com.fixit.platform.modules.request.dto.CreateServiceRequestRequest;
import com.fixit.platform.modules.request.dto.ServiceRequestResponse;
import com.fixit.platform.modules.request.entity.RequestAction;
import com.fixit.platform.modules.request.entity.ServiceRequest;
import com.fixit.platform.modules.request.entity.ServiceRequestSource;
import com.fixit.platform.modules.request.entity.ServiceRequestStatus;
import com.fixit.platform.modules.request.exception.InvalidRequestStateException;
import com.fixit.platform.modules.request.exception.ServiceRequestNotFoundException;
import com.fixit.platform.modules.request.exception.UnauthorizedRequestActionException;
import com.fixit.platform.modules.request.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ProfileRepository profileRepository;
    private final GigRepository gigRepository;


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
        serviceRequest.setSkillId(gig.getSkill().getId());
        serviceRequest.setRequestSource(ServiceRequestSource.DIRECT_GIG);
        serviceRequest.setProposedPrice(gig.getPrice());
        serviceRequest.setAgreedPrice(null);
        serviceRequest.setStatus(ServiceRequestStatus.PENDING);

        serviceRequestRepository.save(serviceRequest);

        return new ApiResponse<>(
                true,
                "Service request created successfully",
                serviceRequest.getId().toString()
        );
    }

    public List<ServiceRequestResponse> getCustomerRequests(
            UUID customerUserId
    ) {
        Profile customerProfile = profileRepository
                .findByUserId(customerUserId)
                .orElseThrow(() ->
                        new RuntimeException("Customer profile not found")
                );

        return serviceRequestRepository
                .findCustomerRequestResponses(customerProfile.getId());
    }

    public List<ServiceRequestResponse> getProviderRequests(
            UUID providerUserId
    ) {
        Profile providerProfile = profileRepository
                .findByUserId(providerUserId)
                .orElseThrow(() ->
                        new RuntimeException("Provider profile not found")
                );

        return serviceRequestRepository
                .findProviderRequestResponses(providerProfile.getId());
    }

    //Action Trasition service

    @Transactional
    public ApiResponse<String> updateRequestStatus(
            UUID userId,
            UUID requestId,
            RequestAction action
    ) {
        Profile actorProfile = profileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new UnauthorizedRequestActionException(
                                "Authenticated profile not found"
                        )
                );

        ServiceRequest serviceRequest = serviceRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new ServiceRequestNotFoundException(
                                "Service request not found"
                        )
                );

        boolean isCustomer =
                actorProfile.getId().equals(
                        serviceRequest.getCustomerProfileId()
                );

        boolean isProvider =
                actorProfile.getId().equals(
                        serviceRequest.getProviderProfileId()
                );

        // The authenticated user must be one of the request participants.
        if (!isCustomer && !isProvider) {
            throw new ServiceRequestNotFoundException(
                    "Service request not found"
            );
        }

        switch (action) {

            case ACCEPT -> acceptRequest(
                    serviceRequest,
                    isProvider
            );

            case REJECT -> rejectRequest(
                    serviceRequest,
                    isProvider
            );

            case FINISH -> finishRequest(
                    serviceRequest,
                    isProvider
            );

            case COMPLETE -> completeRequest(
                    serviceRequest,
                    isCustomer
            );

            case CANCEL -> cancelRequest(
                    serviceRequest,
                    isCustomer
            );
        }

        serviceRequestRepository.save(serviceRequest);

        return new ApiResponse<>(
                true,
                "Request status updated successfully",
                serviceRequest.getStatus().name()
        );
    }

    private void acceptRequest(
            ServiceRequest request,
            boolean isProvider
    ) {
        requireProvider(isProvider);

        requireStatus(
                request,
                ServiceRequestStatus.PENDING,
                "Only pending requests can be accepted"
        );

        request.setAgreedPrice(
                request.getProposedPrice()
        );

        request.setStatus(
                ServiceRequestStatus.IN_PROGRESS
        );
    }

    private void rejectRequest(
            ServiceRequest request,
            boolean isProvider
    ) {
        requireProvider(isProvider);

        requireStatus(
                request,
                ServiceRequestStatus.PENDING,
                "Only pending requests can be rejected"
        );

        request.setStatus(
                ServiceRequestStatus.REJECTED
        );
    }

    private void finishRequest(
            ServiceRequest request,
            boolean isProvider
    ) {
        requireProvider(isProvider);

        requireStatus(
                request,
                ServiceRequestStatus.IN_PROGRESS,
                "Only in-progress requests can be marked as finished"
        );

        request.setStatus(
                ServiceRequestStatus.COMPLETED_BY_PROVIDER
        );
    }

    private void completeRequest(
            ServiceRequest request,
            boolean isCustomer
    ) {
        requireCustomer(isCustomer);

        requireStatus(
                request,
                ServiceRequestStatus.COMPLETED_BY_PROVIDER,
                "The provider must finish the work before customer confirmation"
        );

        request.setStatus(
                ServiceRequestStatus.COMPLETED
        );
    }

    private void cancelRequest(
            ServiceRequest request,
            boolean isCustomer
    ) {
        requireCustomer(isCustomer);

        requireStatus(
                request,
                ServiceRequestStatus.PENDING,
                "Only pending requests can be cancelled"
        );

        request.setStatus(
                ServiceRequestStatus.CANCELLED
        );
    }

    //Validations
    private void requireProvider(boolean isProvider) {

        if (!isProvider) {
            throw new UnauthorizedRequestActionException(
                    "Only the assigned provider can perform this action"
            );
        }
    }

    private void requireCustomer(boolean isCustomer) {

        if (!isCustomer) {
            throw new UnauthorizedRequestActionException(
                    "Only the customer who created this request can perform this action"
            );
        }
    }

    private void requireStatus(
            ServiceRequest request,
            ServiceRequestStatus requiredStatus,
            String message
    ) {
        if (request.getStatus() != requiredStatus) {
            throw new InvalidRequestStateException(
                    message
            );
        }
    }
}
