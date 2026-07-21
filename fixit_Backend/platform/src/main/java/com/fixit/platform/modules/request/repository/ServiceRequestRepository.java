package com.fixit.platform.modules.request.repository;

import com.fixit.platform.modules.request.entity.ServiceRequest;
import com.fixit.platform.modules.request.entity.ServiceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {

    List<ServiceRequest> findByCustomerProfileIdOrderByCreatedAtDesc(
            UUID customerProfileId
    );

    List<ServiceRequest> findByProviderProfileIdOrderByCreatedAtDesc(
            UUID providerProfileId
    );

    Optional<ServiceRequest> findByIdAndCustomerProfileId(
            UUID id,
            UUID customerProfileId
    );

    Optional<ServiceRequest> findByIdAndProviderProfileId(
            UUID id,
            UUID providerProfileId
    );

    boolean existsByCustomerProfileIdAndGigIdAndStatusIn(
            UUID customerProfileId,
            UUID gigId,
            Collection<ServiceRequestStatus> statuses
    );
}
