package com.fixit.platform.modules.request.repository;

import com.fixit.platform.modules.request.dto.ServiceRequestResponse;
import com.fixit.platform.modules.request.entity.ServiceRequest;
import com.fixit.platform.modules.request.entity.ServiceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {


    boolean existsByCustomerProfileIdAndGigIdAndStatusIn(
            UUID customerProfileId,
            UUID gigId,
            Collection<ServiceRequestStatus> statuses
    );

    @Query("""
        SELECT new com.fixit.platform.modules.request.dto.ServiceRequestResponse(
            r.id,
            g.id,
            g.title,
            customer.id,
            customer.fullName,
            provider.id,
            provider.fullName,
            r.jobDescription,
            r.serviceLocation,
            r.scheduledDate,
            r.proposedPrice,
            r.agreedPrice,
            r.status,
            r.createdAt,
            r.updatedAt
        )
        FROM ServiceRequest r
        JOIN Gig g ON g.id = r.gigId
        JOIN Profile customer ON customer.id = r.customerProfileId
        JOIN Profile provider ON provider.id = r.providerProfileId
        WHERE r.customerProfileId = :customerProfileId
        ORDER BY r.createdAt DESC
    """)
    List<ServiceRequestResponse> findCustomerRequestResponses(@Param("customerProfileId") UUID customerProfileId);

    @Query("""
        SELECT new com.fixit.platform.modules.request.dto.ServiceRequestResponse(
            r.id,
            g.id,
            g.title,
            customer.id,
            customer.fullName,
            provider.id,
            provider.fullName,
            r.jobDescription,
            r.serviceLocation,
            r.scheduledDate,
            r.proposedPrice,
            r.agreedPrice,
            r.status,
            r.createdAt,
            r.updatedAt
        )
        FROM ServiceRequest r
        JOIN Gig g ON g.id = r.gigId
        JOIN Profile customer ON customer.id = r.customerProfileId
        JOIN Profile provider ON provider.id = r.providerProfileId
        WHERE r.providerProfileId = :providerProfileId
        ORDER BY r.createdAt DESC
    """)
    List<ServiceRequestResponse> findProviderRequestResponses(
            @Param("providerProfileId") UUID providerProfileId
    );
}
