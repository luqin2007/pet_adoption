package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskRecord;
import com.example.backend.entity.property.RescueTaskAction;
import com.example.backend.entity.property.RescueTaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueTaskUpdateRequest extends LocationRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.rescue_task.summary")
    @Length(max = 20, message = "request.rescue_task.summary")
    private String summary;

    @NotBlank(message = "request.rescue_task.description")
    private String description;

    @NotBlank(message = "request.rescue_task.type")
    private String type;

    public void applyTo(RescueTask task) {
        task.setSummary(summary);
        task.setDescription(description);
        task.setType(RescueTaskType.get(type));
    }

    public RescueTaskRecord createRecord(RescueTask task, Long userId) {
        return new RescueTaskRecord(null,
                task.getId(),
                userId,
                RescueTaskAction.UPDATE,
                task.getStatus(),
                task.getStatus(),
                task.getSummary(),
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, RescueTaskUpdateRequest::getType, RescueTaskType.class, "request.rescue_task.type");
    }
}
