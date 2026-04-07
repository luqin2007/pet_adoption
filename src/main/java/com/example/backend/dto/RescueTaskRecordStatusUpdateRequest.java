package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RescueTaskRecordStatusUpdateRequest implements IRequest {

    @NotBlank(message = "request.rescue_task.status")
    private String status;

    @NotBlank(message = "request.rescue_task.reason")
    private String reason;
}
