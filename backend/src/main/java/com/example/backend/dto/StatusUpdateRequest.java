package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public abstract class StatusUpdateRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.status")
    protected String status;

    @NotBlank(message = "request.reason")
    protected String reason;
}
