package com.fixit.platform.modules.service_request.entity;

import com.fixit.platform.modules.service_request.dao.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ServiceRequest {
    UUID id;

    UUID customerProfileId;

    UUID providerProfileId;

    UUID gigId;

    String description;

    LocalDate requestedDate;

    RequestStatus status;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
