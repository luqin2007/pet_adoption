package com.example.backend.dto;

import com.example.backend.entity.property.VolunteerRewardStatus;
import com.example.backend.entity.property.VolunteerRewardType;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 志愿者激励查询参数
 */
@Data
public class VolunteerRewardQueryParams implements IParam, IValidatedRequest {

    /**
     * 志愿者 id
     */
    private Long volunteer;
    /**
     * 发放人 id
     */
    private Long issuer;
    /**
     * 激励状态集合
     */
    private Set<String> status;
    /**
     * 激励类型集合
     */
    private Set<String> type;
    /**
     * 统计开始时间范围起点
     */
    private Date time0;
    /**
     * 统计开始时间范围终点
     */
    private Date time1;

    /**
     * 校验激励状态、类型和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnums(errors, VolunteerRewardQueryParams::getStatus, VolunteerRewardStatus.class, "request.volunteer.reward.status");
        validateEnums(errors, VolunteerRewardQueryParams::getType, VolunteerRewardType.class, "request.volunteer.reward.type");
        validateTime(errors, VolunteerRewardQueryParams::getTime0, VolunteerRewardQueryParams::getTime1);
    }
}
