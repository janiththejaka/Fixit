package com.fixit.platform.modules.request.dto;

import com.fixit.platform.modules.request.entity.RequestAction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateRequestStatusRequest {

    @NotNull(message = "Request action is required")
    private RequestAction action;
}
