package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskRecord;
import com.example.backend.entity.property.RescueTaskAction;
import com.example.backend.entity.property.RescueTaskStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueTaskRecordStatusUpdateRequest extends StatusUpdateRequest {

    public RescueTaskRecord createRescueTaskRecord(RescueTask task, Long userId, RescueTaskAction action, RescueTaskStatus statusFrom) {
        return new RescueTaskRecord(null,
                task.getId(),
                userId,
                action,
                statusFrom,
                RescueTaskStatus.get(status),
                reason,
                new Date(System.currentTimeMillis()));
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, RescueTaskRecordStatusUpdateRequest::getStatus, RescueTaskStatus.class, "request.status");
    }
}
