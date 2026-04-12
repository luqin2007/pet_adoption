package com.example.backend.dto;

import com.example.backend.entity.property.VolunteerRecruitmentStatus;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 志愿者招募计划查询参数
 */
@Data
public class VolunteerRecruitmentQueryParams implements IParam, IValidatedRequest {

    /**
     * 招募标题
     */
    private String title;
    /**
     * 发布人 id
     */
    private Long publisher;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 招募状态集合
     */
    private Set<String> status;
    /**
     * 招募开始时间范围起点
     */
    private Date time0;
    /**
     * 招募开始时间范围终点
     */
    private Date time1;

    /**
     * 校验状态和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnums(errors, VolunteerRecruitmentQueryParams::getStatus, VolunteerRecruitmentStatus.class, "request.volunteer.recruitment.status");
        validateTime(errors, VolunteerRecruitmentQueryParams::getTime0, VolunteerRecruitmentQueryParams::getTime1);
    }
}
