package com.example.backend.dto;

import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.entity.property.VolunteerTaskType;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 志愿者排班查询参数
 */
@Data
public class VolunteerShiftQueryParams implements IParam, IValidatedRequest {

    /**
     * 志愿者 id
     */
    private Long volunteer;
    /**
     * 排班人 id
     */
    private Long assigner;
    /**
     * 关键字
     */
    private String keyword;
    /**
     * 任务类型集合
     */
    private Set<String> taskType;
    /**
     * 状态集合
     */
    private Set<String> status;
    /**
     * 开始时间范围起点
     */
    private Date time0;
    /**
     * 开始时间范围终点
     */
    private Date time1;

    /**
     * 校验任务类型、状态和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnums(errors, VolunteerShiftQueryParams::getTaskType, VolunteerTaskType.class, "request.volunteer.shift.type");
        validateEnums(errors, VolunteerShiftQueryParams::getStatus, VolunteerShiftStatus.class, "request.volunteer.shift.status");
        validateTime(errors, VolunteerShiftQueryParams::getTime0, VolunteerShiftQueryParams::getTime1);
    }
}
