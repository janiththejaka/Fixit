package com.fixit.platform.modules.service_request.dto;

import com.fixit.platform.modules.service_request.dao.RequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;


@Getter
@Setter
public class ServiceRequestResponse {
    private UUID requestId;

    private UUID gigId;

    private String gigTitle;

    private String customerName;

    private String providerName;

    private String description;

    private RequestStatus status;

    private LocalDate requestedDate;
}
