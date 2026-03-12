package com.example.backend.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import static com.example.backend.util.C.RESCUE_TASK_STATUS_MAX;
import static com.example.backend.util.C.RESCUE_TASK_STATUS_MIN;

@Data
public class RescueTaskRecordRequest {

    @Range(min = RESCUE_TASK_STATUS_MIN, max = RESCUE_TASK_STATUS_MAX, message = "异常状态")
    private Integer status;

    private String reason;
}
