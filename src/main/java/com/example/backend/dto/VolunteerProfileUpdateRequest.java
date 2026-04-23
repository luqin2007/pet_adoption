package com.example.backend.dto;

import com.example.backend.entity.VolunteerProfile;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 志愿者档案更新请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerProfileUpdateRequest extends LocationRequest implements IRequest {

    @NotBlank(message = "request.volunteer.profile.real_name")
    private String realName;
    @NotBlank(message = "request.volunteer.profile.sex")
    private String sex;
    @NotBlank(message = "request.volunteer.profile.phone")
    private String phone;
    private String skills;
    private String serviceIntention;
    private String availableTimeDesc;
    private String remark;

    /**
     * 将请求内容应用到志愿者档案
     */
    public void applyTo(VolunteerProfile profile) {
        profile.setRealName(realName);
        profile.setSex(sex);
        profile.setPhone(phone);
        profile.setSkills(skills);
        profile.setServiceDesc(serviceIntention);
        profile.setTimeDesc(availableTimeDesc);
        profile.setRemark(remark);
        profile.setUpdateTime(new Date());
    }
}
