package com.example.backend.dto;

import com.example.backend.entity.property.VolunteerApplicationStatus;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Set;

/**
 * 志愿者申请查询参数
 */
@Data
public class VolunteerApplicationQueryParams implements IParam, IValidatedRequest {

    /**
     * 招募计划 id
     */
    private Long recruitment;
    /**
     * 申请人 id
     */
    private Long user;
    /**
     * 审核人 id
     */
    private Long reviewer;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 申请状态集合
     */
    private Set<String> status;

    /**
     * 校验申请状态
     */
    @Override
    public void validate(Errors errors) {
        validateEnums(errors, VolunteerApplicationQueryParams::getStatus, VolunteerApplicationStatus.class, "request.volunteer.application.status");
    }
}
