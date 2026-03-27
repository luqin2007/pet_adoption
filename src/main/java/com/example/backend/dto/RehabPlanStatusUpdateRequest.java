package com.example.backend.dto;

import com.example.backend.entity.RehabPlanStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class RehabPlanStatusUpdateRequest {

    @NotBlank(message = "请输入状态")
    private String status;

    @NotBlank(message = "请填写原因")
    private String reason;

    public RehabPlanStatus create(Long planId, Long userId) {
        return new RehabPlanStatus(null,
                planId,
                userId,
                com.example.backend.entity.property.RehabPlanStatus.get(status),
                reason,
                new Date());
    }
}
