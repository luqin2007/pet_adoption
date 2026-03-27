package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RescueTaskRecordStatusUpdateRequest {

    @NotBlank(message = "异常状态")
    private String status;

    @NotBlank(message = "请输入原因")
    private String reason;
}
