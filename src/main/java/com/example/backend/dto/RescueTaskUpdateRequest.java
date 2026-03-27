package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.property.RescueTaskType;
import com.example.backend.util.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueTaskUpdateRequest extends LocationRequest {

    /**
     * 任务简介
     */
    private String summary;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 任务类型
     */
    private String type;

    private String reason;

    public void applyTo(RescueTask task) {
        if (StringUtils.hasText(summary)) task.setSummary(summary);
        if (StringUtils.hasText(description)) task.setDescription(description);
        if (StringUtils.hasText(type)) task.setType(RescueTaskType.get(type));
    }
}
