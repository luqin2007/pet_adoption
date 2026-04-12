package com.example.backend.dto;

import com.example.backend.entity.VolunteerRecruitment;
import com.example.backend.entity.property.VolunteerRecruitmentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 志愿者招募计划创建请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerRecruitmentAddRequest extends LocationRequest implements IRequest, IValidatedRequest {

    /**
     * 招募标题
     */
    @NotBlank(message = "request.volunteer.recruitment.title")
    private String title;

    /**
     * 招募说明
     */
    @NotBlank(message = "request.volunteer.recruitment.description")
    private String description;

    /**
     * 招募要求
     */
    @NotBlank(message = "request.volunteer.recruitment.requirement")
    private String requirement;

    /**
     * 招募人数
     */
    @NotNull(message = "request.volunteer.recruitment.count")
    @Min(value = 1, message = "request.volunteer.recruitment.count.min")
    private Integer headcount;

    /**
     * 招募开始时间
     */
    @NotNull(message = "request.volunteer.recruitment.time")
    private Date startTime;

    /**
     * 招募结束时间
     */
    @NotNull(message = "request.volunteer.recruitment.time")
    private Date endTime;

    /**
     * 创建招募计划实体
     */
    public VolunteerRecruitment create(Long publisherId) {
        Date now = new Date();
        return new VolunteerRecruitment(
                null,
                title,
                description,
                requirement,
                detailAddress,
                province,
                city,
                district,
                headcount,
                0,
                startTime,
                endTime,
                VolunteerRecruitmentStatus.DRAFT,
                publisherId,
                now,
                now);
    }

    /**
     * 校验
     */
    @Override
    public void validate(Errors errors) {
        validateTime(errors, VolunteerRecruitmentAddRequest::getStartTime, VolunteerRecruitmentAddRequest::getEndTime);
    }
}
