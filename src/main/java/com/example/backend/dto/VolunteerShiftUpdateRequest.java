package com.example.backend.dto;

import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.property.VolunteerTaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者排班更新请求
 */
@Data
public class VolunteerShiftUpdateRequest implements IRequest, IValidatedRequest {

    @NotNull(message = "request.volunteer.shift.volunteer")
    private Long volunteerId;
    @NotBlank(message = "request.volunteer.shift.type")
    private String taskType;
    private Long taskSourceId;
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
     * 将请求内容应用到排班实体
     */
    public void applyTo(VolunteerShift shift) {
        shift.setVolunteerId(volunteerId);
        shift.setTaskType(VolunteerTaskType.get(taskType));
        shift.setTaskSourceId(taskSourceId);
        shift.setTitle(title);
        shift.setContent(content);
        shift.setServiceAddress(serviceAddress);
        shift.setProvince(province);
        shift.setCity(city);
        shift.setDistrict(district);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setEstimatedHours(estimatedHours);
        shift.setRemark(remark);
        shift.setUpdateTime(new Date());
    }

    /**
     * 校验任务类型和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerShiftUpdateRequest::getTaskType, VolunteerTaskType.class, "request.volunteer.shift.type");
        validateTime(errors, VolunteerShiftUpdateRequest::getStartTime, VolunteerShiftUpdateRequest::getEndTime);
    }
}
