package com.example.backend.dto;

import com.example.backend.entity.RehabPlanStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.Date;

import static com.example.backend.util.C.REHAB_PLAN_STATUS_MAX;
import static com.example.backend.util.C.REHAB_PLAN_STATUS_MIN;

@Data
public class RehabPlanStatusUpdateRequest {

    @Range(min = REHAB_PLAN_STATUS_MIN, max = REHAB_PLAN_STATUS_MAX, message = "无效状态")
    private Integer status;

    @NotBlank(message = "请填写原因")
    private String reason;

    public RehabPlanStatus create(Long planId, Long userId) {
        return new RehabPlanStatus(null,
                planId,
                userId,
                status,
                reason,
                new Date());
    }
}
