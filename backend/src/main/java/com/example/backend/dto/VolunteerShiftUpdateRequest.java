package com.example.backend.dto;

import com.example.backend.entity.VolunteerShift;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 志愿者排班更新请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerShiftUpdateRequest extends LocationRequest implements IRequest, IValidatedRequest {

    @NotNull(message = "request.volunteer.shift.volunteer")
    private Long volunteerId;
    @NotNull(message = "request.volunteer.shift.time")
    private Date startTime;
    @NotNull(message = "request.volunteer.shift.time")
    private Date endTime;

    /**
     * 将请求内容应用到排班实体
     */
    public void applyTo(VolunteerShift shift) {
        shift.setVolunteerId(volunteerId);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setUpdateTime(new Date());
    }

    /**
     * 校验任务类型和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateTime(errors, VolunteerShiftUpdateRequest::getStartTime, VolunteerShiftUpdateRequest::getEndTime);
    }
}
