package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskRecord;
import com.example.backend.entity.property.RescueTaskAction;
import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.entity.property.RescueTaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueTaskAddRequest extends LocationRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.timeout")
    private String id;

    /**
     * 上一个任务 id
     */
    private Long previousId;

    /**
     * 任务简介
     */
    @NotBlank(message = "request.rescue_task.summary")
    private String summary;

    /**
     * 详细描述
     */
    @NotBlank(message = "request.rescue_task.description")
    private String description;

    /**
     * 任务类型
     */
    @NotBlank(message = "request.rescue_task.type")
    private String type;

    public RescueTask createTask(Long userId) {
        Date now = new Date(System.currentTimeMillis());
        return new RescueTask(null,
                previousId,
                userId,
                null,
                summary,
                description,
                RescueTaskStatus.CREATED,
                RescueTaskType.get(type),
                now,
                now);
    }

    public RescueTaskRecord createRecord(RescueTask task, Long userId) {
        return new RescueTaskRecord(null,
                task.getId(),
                userId,
                RescueTaskAction.CREATE,
                RescueTaskStatus.CREATED,
                task.getStatus(),
                task.getSummary(),
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, RescueTaskAddRequest::getType, RescueTaskType.class, "request.rescue_task.type");
    }
}
