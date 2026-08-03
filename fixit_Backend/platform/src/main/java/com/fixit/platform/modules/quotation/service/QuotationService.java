package com.fixit.platform.modules.quotation.service;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.entity.Skill;
import com.fixit.platform.modules.profile.repository.ProfileRepository;
import com.fixit.platform.modules.profile.repository.ProviderSkillRepository;
import com.fixit.platform.modules.profile.repository.SkillRepository;
import com.fixit.platform.modules.quotation.dto.CreateQuotationRequest;
import com.fixit.platform.modules.quotation.dto.QuotationResponse;
import com.fixit.platform.modules.quotation.dto.RespondQuotationRequest;
import com.fixit.platform.modules.quotation.entity.QuotationRequest;
import com.fixit.platform.modules.quotation.entity.QuotationStatus;
import com.fixit.platform.modules.quotation.repository.QuotationRepository;
import com.fixit.platform.modules.request.entity.ServiceRequest;
import com.fixit.platform.modules.request.entity.ServiceRequestSource;
import com.fixit.platform.modules.request.entity.ServiceRequestStatus;
import com.fixit.platform.modules.request.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuotationService {

    private final QuotationRepository quotationRepository;

    private final ProfileRepository profileRepository;
    private final ProviderSkillRepository providerSkillRepository;
    private final SkillRepository skillRepository;

    private final ServiceRequestRepository serviceRequestRepository;

    /*
     * CLIENT:
     * Creates a quotation request for one skill belonging to a provider.
     */
    @Transactional
    public ApiResponse<String> createQuotation(UUID customerUserId, CreateQuotationRequest request)
    {
        Profile customerProfile = getProfileByUserId(
                customerUserId,
                "Customer profile not found"
        );

        Profile providerProfile = profileRepository
                .findById(request.getProviderProfileId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Provider profile not found"
                        )
                );

        if (!providerProfile.isProviderProfileComplete()) {
            throw new RuntimeException(
                    "Provider is currently unavailable"
            );
        }

        if (customerProfile.getId()
                .equals(providerProfile.getId())) {

            throw new RuntimeException(
                    "You cannot request a quotation from yourself"
            );
        }

        Skill skill = skillRepository
                .findById(request.getSkillId())
                .orElseThrow(() ->
                        new RuntimeException("Skill not found")
                );

        boolean providerHasSkill =
                providerSkillRepository
                        .existsByProfileIdAndSkillId(
                                providerProfile.getId(),
                                skill.getId()
                        );

        if (!providerHasSkill) {
            throw new RuntimeException(
                    "Selected skill is not offered by this provider"
            );
        }

        boolean duplicateActiveQuotation =
                quotationRepository
                        .existsByCustomerProfileIdAndProviderProfileIdAndSkillIdAndStatusIn(
                                customerProfile.getId(),
                                providerProfile.getId(),
                                skill.getId(),
                                List.of(
                                        QuotationStatus.PENDING,
                                        QuotationStatus.QUOTED
                                )
                        );

        if (duplicateActiveQuotation) {
            throw new RuntimeException(
                    "You already have an active quotation request " +
                            "for this provider and skill"
            );
        }

        QuotationRequest quotation = new QuotationRequest();

        quotation.setCustomerProfileId(customerProfile.getId());
        quotation.setProviderProfileId(providerProfile.getId());
        quotation.setSkillId(skill.getId());
        quotation.setJobDescription(request.getJobDescription());
        quotation.setServiceLocation(request.getServiceLocation());
        quotation.setScheduledDate(request.getScheduledDate());
        quotation.setCustomerNote(request.getCustomerNote());
        quotation.setQuotedPrice(null);
        quotation.setProviderMessage(null);
        quotation.setServiceRequestId(null);
        quotation.setStatus(QuotationStatus.PENDING);

        quotationRepository.save(quotation);

        return new ApiResponse<>(
                true,
                "Quotation request sent successfully",
                quotation.getId().toString()
        );
    }

    /*
     * PROVIDER:
     * Responds to a quotation request with price and message.
     */
    @Transactional
    public ApiResponse<String> respondQuotation(
            UUID providerUserId,
            UUID quotationId,
            RespondQuotationRequest request
    ) {
        Profile providerProfile = getProfileByUserId(
                providerUserId,
                "Provider profile not found"
        );

        QuotationRequest quotation =
                quotationRepository
                        .findByIdAndProviderProfileId(
                                quotationId,
                                providerProfile.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quotation request not found"
                                )
                        );

        if (quotation.getStatus()
                != QuotationStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending quotation requests can be answered"
            );
        }

        quotation.setQuotedPrice(
                request.getQuotedPrice()
        );

        quotation.setProviderMessage(
                request.getProviderMessage()
        );

        quotation.setStatus(
                QuotationStatus.QUOTED
        );

        quotation.setRespondedAt(
                LocalDateTime.now()
        );

        quotationRepository.save(quotation);

        return new ApiResponse<>(
                true,
                "Quotation submitted successfully",
                quotation.getStatus().name()
        );
    }

    /*
     * CLIENT:
     * Accepts provider quotation and creates the real ServiceRequest.
     *
     * Both operations happen in one transaction.
     */
    @Transactional
    public ApiResponse<String> acceptQuotation(
            UUID customerUserId,
            UUID quotationId
    ) {
        Profile customerProfile = getProfileByUserId(
                customerUserId,
                "Customer profile not found"
        );

        QuotationRequest quotation =
                quotationRepository
                        .findByIdAndCustomerProfileId(
                                quotationId,
                                customerProfile.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quotation request not found"
                                )
                        );

        if (quotation.getStatus()
                != QuotationStatus.QUOTED) {

            throw new RuntimeException(
                    "Only quoted requests can be accepted"
            );
        }

        if (quotation.getQuotedPrice() == null) {
            throw new RuntimeException(
                    "Quotation does not contain a valid price"
            );
        }

        if (quotation.getServiceRequestId() != null) {
            throw new RuntimeException(
                    "A service request has already been created " +
                            "from this quotation"
            );
        }

        Profile providerProfile = profileRepository
                .findById(quotation.getProviderProfileId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Provider profile not found"
                        )
                );

        if (!providerProfile.isProviderProfileComplete()) {
            throw new RuntimeException(
                    "Provider is currently unavailable"
            );
        }

        boolean providerStillHasSkill =
                providerSkillRepository
                        .existsByProfileIdAndSkillId(
                                providerProfile.getId(),
                                quotation.getSkillId()
                        );

        if (!providerStillHasSkill) {
            throw new RuntimeException(
                    "Provider no longer offers this skill"
            );
        }

        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setCustomerProfileId(quotation.getCustomerProfileId());
        serviceRequest.setProviderProfileId(quotation.getProviderProfileId());

        // A custom quotation may not belong to a fixed gig.
        serviceRequest.setGigId(null);

        serviceRequest.setSkillId(quotation.getSkillId());
        serviceRequest.setRequestSource(ServiceRequestSource.QUOTATION);
        serviceRequest.setJobDescription(quotation.getJobDescription());
        serviceRequest.setServiceLocation(quotation.getServiceLocation());
        serviceRequest.setScheduledDate(quotation.getScheduledDate());

        /*
         * Provider proposed this price and the customer accepted it,
         * so it is already the agreed price.
         */
        serviceRequest.setProposedPrice(quotation.getQuotedPrice());
        serviceRequest.setAgreedPrice(quotation.getQuotedPrice());

        /*
         * Both sides have agreed:
         * provider sent quotation and customer accepted it.
         */
        serviceRequest.setStatus(ServiceRequestStatus.IN_PROGRESS);
        ServiceRequest createdRequest = serviceRequestRepository.save(serviceRequest);
        quotation.setStatus(QuotationStatus.ACCEPTED);
        quotation.setServiceRequestId(createdRequest.getId());

        quotationRepository.save(quotation);

        return new ApiResponse<>(
                true,
                "Quotation accepted and service request created",
                createdRequest.getId().toString()
        );
    }

    @Transactional(readOnly = true)
    public List<QuotationResponse> getCustomerQuotations(
            UUID customerUserId
    ) {
        Profile customerProfile = getProfileByUserId(
                customerUserId,
                "Customer profile not found"
        );

        return quotationRepository
                .findCustomerQuotationResponses(
                        customerProfile.getId()
                );
    }

    @Transactional(readOnly = true)
    public List<QuotationResponse> getProviderQuotations(
            UUID providerUserId
    ) {
        Profile providerProfile = getProfileByUserId(
                providerUserId,
                "Provider profile not found"
        );

        return quotationRepository
                .findProviderQuotationResponses(
                        providerProfile.getId()
                );
    }

    private Profile getProfileByUserId(UUID userId, String errorMessage)
    {
        return profileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(errorMessage)
                );
    }

}
