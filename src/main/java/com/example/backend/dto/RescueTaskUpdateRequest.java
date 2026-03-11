package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

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
    @Range(min = 0, max = 2, message = "未知类型")
    private Integer type;

    private String reason;

    public void applyTo(RescueTask task) {
        if (StringUtils.hasText(summary)) task.setSummary(summary);
        if (description != null) task.setDescription(description);
        if (type != null) task.setType(type);
    }
}
