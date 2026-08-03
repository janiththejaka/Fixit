package com.fixit.platform.modules.quotation.repository;

import com.fixit.platform.modules.quotation.dto.QuotationResponse;
import com.fixit.platform.modules.quotation.entity.QuotationRequest;
import com.fixit.platform.modules.quotation.entity.QuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRepository extends JpaRepository<QuotationRequest, UUID> {

    @Query("""
        SELECT new com.fixit.platform.modules.quotation.dto.QuotationResponse(
            q.id,

            provider.id,
            customer.id,

            provider.fullName,
            customer.fullName,

            skill.id,
            skill.name,

            q.jobDescription,
            q.serviceLocation,
            q.scheduledDate,
            q.customerNote,

            q.quotedPrice,
            q.providerMessage,

            q.status,

            q.serviceRequestId,

            q.createdAt,
            q.respondedAt
        )
        FROM QuotationRequest q
        JOIN Profile provider
            ON provider.id = q.providerProfileId
        JOIN Profile customer
            ON customer.id = q.customerProfileId
        JOIN Skill skill
            ON skill.id = q.skillId
        WHERE q.customerProfileId = :customerProfileId
        ORDER BY q.createdAt DESC
    """)
    List<QuotationResponse> findCustomerQuotationResponses(@Param("customerProfileId") UUID customerProfileId);

    @Query("""
        SELECT new com.fixit.platform.modules.quotation.dto.QuotationResponse(
            q.id,

            provider.id,
            customer.id,

            provider.fullName,
            customer.fullName,

            skill.id,
            skill.name,

            q.jobDescription,
            q.serviceLocation,
            q.scheduledDate,
            q.customerNote,

            q.quotedPrice,
            q.providerMessage,

            q.status,

            q.serviceRequestId,

            q.createdAt,
            q.respondedAt
        )
        FROM QuotationRequest q
        JOIN Profile provider
            ON provider.id = q.providerProfileId
        JOIN Profile customer
            ON customer.id = q.customerProfileId
        JOIN Skill skill
            ON skill.id = q.skillId
        WHERE q.providerProfileId = :providerProfileId
        ORDER BY q.createdAt DESC
    """)
    List<QuotationResponse> findProviderQuotationResponses(@Param("providerProfileId") UUID providerProfileId);

    Optional<QuotationRequest> findByIdAndProviderProfileId(UUID quotationId, UUID providerProfileId);

    Optional<QuotationRequest> findByIdAndCustomerProfileId(UUID quotationId, UUID customerProfileId);

    boolean existsByCustomerProfileIdAndProviderProfileIdAndSkillIdAndStatusIn(
            UUID customerProfileId,
            UUID providerProfileId,
            UUID skillId,
            Collection<QuotationStatus> statuses
    );
}
