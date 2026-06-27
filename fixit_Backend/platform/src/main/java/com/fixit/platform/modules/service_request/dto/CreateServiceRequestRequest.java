package com.fixit.platform.modules.service_request.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;


@Getter
@Setter
public class CreateServiceRequestRequest {

    private UUID gigId;

    private String description;

    private LocalDate requestedDate;
}
