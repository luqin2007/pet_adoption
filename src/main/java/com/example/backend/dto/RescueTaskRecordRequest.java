package com.example.backend.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class RescueTaskRecordRequest {

    @Range(min = 1, max = 5, message = "异常状态")
    private Integer status;

    private String reason;
}
