package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.entity.property.RescueTaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueTaskAddRequest extends LocationRequest {

    /**
     * 临时 uuid
     */
    @NotBlank
    private String id;

    /**
     * 上一个任务 id
     */
    private Long previousId;

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
    @NotBlank(message = "请输入任务类型")
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
}
