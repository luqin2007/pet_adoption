package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

import static com.example.backend.util.C.RESCUE_TASK_TYPE_MAX;
import static com.example.backend.util.C.RESCUE_TASK_TYPE_MIN;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueTaskUpdateRequest extends LocationRequest {

    /**
     * 任务简介
     */
    @NotBlank
    private String summary;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 任务类型
     */
    @Range(min = RESCUE_TASK_TYPE_MIN, max = RESCUE_TASK_TYPE_MAX, message = "未知类型")
    private Integer type;

    private String reason;

    public void applyTo(RescueTask task) {
        if (StringUtils.hasText(summary)) task.setSummary(summary);
        if (description != null) task.setDescription(description);
        if (type != null) task.setType(type);
    }
}
