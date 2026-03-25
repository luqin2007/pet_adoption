package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

import java.util.Date;

import static com.example.backend.util.C.RESCUE_TASK_TYPE_MAX;
import static com.example.backend.util.C.RESCUE_TASK_TYPE_MIN;

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
    @Range(min = RESCUE_TASK_TYPE_MIN, max = RESCUE_TASK_TYPE_MAX, message = "未知类型")
    private Integer type;

    public RescueTask createTask(Long userId) {
        Date now = new Date(System.currentTimeMillis());
        return new RescueTask(null,
                previousId,
                userId,
                null,
                summary,
                description,
                0,
                type,
                now,
                now);
    }
}
