package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.property.RescueTaskType;
import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueTaskUpdateRequest extends LocationRequest {

    @NotBlank(message = "请输入任务简介")
    private String summary;

    @NotBlank(message = "请输入详细描述")
    private String description;

    @NotBlank(message = "请输入任务类型")
    private String type;

    public void applyTo(RescueTask task) {
        if (StringUtils.hasText(summary)) task.setSummary(summary);
        if (StringUtils.hasText(description)) task.setDescription(description);
        if (StringUtils.hasText(type)) task.setType(RescueTaskType.get(type));
    }
}
