package com.example.backend.dto;

import com.example.backend.entity.VolunteerRecruitment;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 志愿者招募计划更新请求
 */
@Data
public class VolunteerRecruitmentUpdateRequest implements IRequest, IValidatedRequest {

    private String title;
    private String description;
    private String requirement;
    private String serviceAddress;
    private String province;
    private String city;
    private String district;
    @Min(value = 1, message = "招募人数必须大于 0")
    private Integer headcount;
    private Date startTime;
    private Date endTime;

    /**
     * 将请求内容应用到招募计划实体
     */
    public void applyTo(VolunteerRecruitment recruitment) {
        if (title != null) recruitment.setTitle(title);
        if (description != null) recruitment.setDescription(description);
        if (requirement != null) recruitment.setRequirement(requirement);
        if (serviceAddress != null) recruitment.setServiceAddress(serviceAddress);
        if (province != null) recruitment.setProvince(province);
        if (city != null) recruitment.setCity(city);
        if (district != null) recruitment.setDistrict(district);
        if (headcount != null) recruitment.setHeadcount(headcount);
        if (startTime != null) recruitment.setStartTime(startTime);
        if (endTime != null) recruitment.setEndTime(endTime);
        recruitment.setUpdateTime(new Date());
    }

    /**
     * 校验时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateTime(errors, VolunteerRecruitmentUpdateRequest::getStartTime, VolunteerRecruitmentUpdateRequest::getEndTime);
    }
}
