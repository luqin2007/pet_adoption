package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.property.RescueTaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueTaskUpdateRequest extends LocationRequest implements IRequest, IRequestValidate {

    @NotBlank(message = "request.rescue_task.summary")
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

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, RescueTaskUpdateRequest::getType, RescueTaskType.class, "request.rescue_task.type");
    }
}
