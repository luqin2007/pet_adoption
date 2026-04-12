package com.example.backend.dto;

import com.example.backend.entity.VolunteerApplication;
import com.example.backend.entity.property.VolunteerApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 志愿者申请创建请求
 */
@Data
public class VolunteerApplicationAddRequest implements IRequest {

    /**
     * 招募计划 id
     */
    @NotNull(message = "招募计划不能为空")
    private Long recruitmentId;
    /**
     * 真实姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    private String phone;
    private String sex;
    private Integer age;
    private String profession;
    private String province;
    private String city;
    private String district;
    private String address;
    private String experience;
    private String skills;
    private String availableTimeDesc;
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
                province,
                city,
                district,
                address,
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
}
