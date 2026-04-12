package com.example.backend.dto;

import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.entity.property.VolunteerTaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者排班创建请求
 */
@Data
public class VolunteerShiftAddRequest implements IRequest, IValidatedRequest {

    /**
     * 志愿者 id
     */
    @NotNull(message = "request.volunteer.shift.volunteer")
    private Long volunteerId;
    /**
     * 任务类型
     */
    @NotBlank(message = "request.volunteer.shift.type")
    private String taskType;
    /**
     * 任务来源 id
     */
    private Long taskSourceId;
    /**
     * 任务标题
     */
    @NotBlank(message = "request.volunteer.shift.title")
    private String title;
    private String content;
    private String serviceAddress;
    private String province;
    private String city;
    private String district;
    @NotNull(message = "request.volunteer.shift.time")
    private Date startTime;
    @NotNull(message = "request.volunteer.shift.time")
    private Date endTime;
    private BigDecimal estimatedHours;
    private String remark;

    /**
     * 创建排班实体
     */
    public VolunteerShift create(Long assignerId) {
        Date now = new Date();
        return new VolunteerShift(
                null,
                volunteerId,
                assignerId,
                VolunteerTaskType.get(taskType),
                taskSourceId,
                title,
                content,
                serviceAddress,
                province,
                city,
                district,
                startTime,
                endTime,
                estimatedHours,
                VolunteerShiftStatus.ASSIGNED,
                remark,
                now,
                now);
    }

    /**
     * 校验任务类型和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerShiftAddRequest::getTaskType, VolunteerTaskType.class, "request.volunteer.shift.type");
        validateTime(errors, VolunteerShiftAddRequest::getStartTime, VolunteerShiftAddRequest::getEndTime);
    }
}
