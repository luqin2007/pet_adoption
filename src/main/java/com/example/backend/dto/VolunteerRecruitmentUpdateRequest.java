package com.example.backend.dto;

import com.example.backend.entity.VolunteerRecruitment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 志愿者招募计划更新请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerRecruitmentUpdateRequest extends LocationRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.volunteer.recruitment.title")
    private String title;
    @NotBlank(message = "request.volunteer.recruitment.description")
    private String description;
    @NotBlank(message = "request.volunteer.recruitment.requirement")
    private String requirement;
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
