package com.example.backend.dto;

import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.entity.property.RescueTaskType;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Set;

@Data
public class RescueTaskQueryParams implements IParam, IValidatedRequest {

    private Set<Long> user;

    private Set<String> status;

    private Set<String> type;

    private String keyword;

    @Override
    public void validate(Errors errors) {
        validateEnums(errors, RescueTaskQueryParams::getStatus, RescueTaskStatus.class, "request.rescue_task_status");
        validateEnums(errors, RescueTaskQueryParams::getType, RescueTaskType.class, "request.rescue_task.type");
    }
}
