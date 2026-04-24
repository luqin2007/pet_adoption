package com.example.backend.dto;

import com.example.backend.entity.RehabPlanStatus;
import com.example.backend.entity.property.RehabPlanStatusProp;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class RehabPlanStatusUpdateRequest extends StatusUpdateRequest {

    public RehabPlanStatus create(Long planId, Long userId) {
        return new RehabPlanStatus(null,
                userId,
                planId,
                RehabPlanStatusProp.get(status),
                reason,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, RehabPlanStatusUpdateRequest::getStatus, RehabPlanStatusProp.class, "request.medical.rehab.task.status");
    }
}
