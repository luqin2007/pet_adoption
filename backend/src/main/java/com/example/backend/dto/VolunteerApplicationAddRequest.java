package com.example.backend.dto;

import com.example.backend.entity.VolunteerApplication;
import com.example.backend.entity.property.VolunteerApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 志愿者申请创建请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerApplicationAddRequest extends LocationRequest implements IRequest, IValidatedRequest {

    /**
     * 招募计划 id
     */
    @NotNull(message = "request.volunteer.application.recruitment")
    private Long recruitmentId;

    /**
     * 真实姓名
     */
    @NotBlank(message = "request.volunteer.application.real_name")
    private String realName;

    /**
     * 联系电话
     */
    @NotBlank(message = "request.volunteer.application.phone")
    private String phone;

    /**
     * 性别
     */
    @NotBlank(message = "request.volunteer.application.sex")
    private String sex;

    private Integer age;
    private String profession;

    /**
     * 过往经历
     */
    @NotBlank(message = "request.volunteer.application.experience")
    private String experience;

    /**
     * 技能
     */
    @NotBlank(message = "request.volunteer.application.skills")
    private String skills;

    /**
     * 可服务时间说明
     */
    @NotBlank(message = "request.volunteer.application.available_time")
    private String availableTimeDesc;

    /**
     * 申请动机
     */
    @NotBlank(message = "request.volunteer.application.motivation")
    private String motivation;

    /**
     * 创建申请实体
     */
    public VolunteerApplication create(Long userId) {
        Date now = new Date();
        return new VolunteerApplication(null,
                recruitmentId,
                userId,
                realName,
                sex,
                phone,
                age,
                experience,
                skills,
                availableTimeDesc,
                motivation,
                VolunteerApplicationStatus.SUBMITTED,
                null,
                null,
                null,
                now,
                now);
    }

    /**
     * 复用基础校验
     */
    @Override
    public void validate(Errors errors) {
    }
}
