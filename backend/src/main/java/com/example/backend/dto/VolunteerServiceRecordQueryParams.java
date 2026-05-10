package com.example.backend.dto;

import com.example.backend.entity.property.VolunteerRecordStatus;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 志愿者服务记录查询参数
 */
@Data
public class VolunteerServiceRecordQueryParams implements IParam, IValidatedRequest {

    /**
     * 志愿者 id
     */
    private Long volunteer;
    /**
     * 排班 id
     */
    private Long shift;
    /**
     * 审核人 id
     */
    private Long reviewer;
    /**
     * 状态集合
     */
    private Set<String> status;
    /**
     * 服务开始时间范围起点
     */
    private Date time0;
    /**
     * 服务开始时间范围终点
     */
    private Date time1;

    /**
     * 校验记录状态和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnums(errors, VolunteerServiceRecordQueryParams::getStatus, VolunteerRecordStatus.class, "request.volunteer.record.status");
        validateTime(errors, VolunteerServiceRecordQueryParams::getTime0, VolunteerServiceRecordQueryParams::getTime1);
    }
}
