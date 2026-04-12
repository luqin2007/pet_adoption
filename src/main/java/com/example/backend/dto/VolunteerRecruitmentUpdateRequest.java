package com.example.backend.dto;

import com.example.backend.entity.VolunteerRecruitment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 志愿者招募计划更新请求
 */
@Data
public class VolunteerRecruitmentUpdateRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.volunteer.recruitment.title")
    private String title;
    @NotBlank(message = "request.volunteer.recruitment.description")
    private String description;
    @NotBlank(message = "request.volunteer.recruitment.requirement")
    private String requirement;
    private String serviceAddress;
    private String province;
    private String city;
    private String district;
    @NotNull(message = "request.volunteer.recruitment.count")
    @Min(value = 1, message = "request.volunteer.recruitment.count.min")
    private Integer headcount;
    @NotNull(message = "request.volunteer.recruitment.time")
    private Date startTime;
    @NotNull(message = "request.volunteer.recruitment.time")
    private Date endTime;

    /**
     * 将请求内容应用到招募计划实体
     */
    public void applyTo(VolunteerRecruitment recruitment) {
        recruitment.setTitle(title);
        recruitment.setDescription(description);
        recruitment.setRequirement(requirement);
        recruitment.setServiceAddress(serviceAddress);
        recruitment.setProvince(province);
        recruitment.setCity(city);
        recruitment.setDistrict(district);
        recruitment.setHeadcount(headcount);
        recruitment.setStartTime(startTime);
        recruitment.setEndTime(endTime);
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
