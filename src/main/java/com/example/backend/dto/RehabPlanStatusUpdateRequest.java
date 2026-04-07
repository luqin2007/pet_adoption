package com.example.backend.dto;

import com.example.backend.entity.RehabPlanStatus;
import com.example.backend.entity.property.RehabPlanStatusProp;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class RehabPlanStatusUpdateRequest implements IRequest, IRequestValidate {

    @NotBlank(message = "request.medical.rehab.task.status")
    private String status;

    @NotBlank(message = "request.medical.rehab.task.reason")
    private String reason;

    public RehabPlanStatus create(Long planId, Long userId) {
        return new RehabPlanStatus(null,
                planId,
                userId,
                RehabPlanStatusProp.get(status),
                reason,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, RehabPlanStatusUpdateRequest::getStatus, RehabPlanStatusProp.class, "request.medical.rehab.task.status");
    }
}
