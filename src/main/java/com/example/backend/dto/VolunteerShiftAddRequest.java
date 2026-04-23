package com.example.backend.dto;

import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.VolunteerTask;
import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.entity.property.VolunteerTaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者排班创建请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerShiftAddRequest extends LocationRequest implements IRequest, IValidatedRequest {

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
    private Long taskId;
    /**
     * 任务标题
     */
    @NotBlank(message = "request.volunteer.shift.title")
    private String title;
    private String content;
    @NotNull(message = "request.volunteer.shift.time")
    private Date startTime;
    @NotNull(message = "request.volunteer.shift.time")
    private Date endTime;
    private Date taskStartTime;
    private Date taskEndTime;
    private String remark;

    /**
     * 创建排班实体
     */
    public VolunteerShift create(Long assignerId) {
        Date now = new Date();
        return new VolunteerShift(null,
                volunteerId,
                assignerId,
                taskId,
                VolunteerShiftStatus.ASSIGNED,
                remark,
                startTime,
                endTime,
                now,
                now);
    }

    public VolunteerTask createTask() {
        return new VolunteerTask(null,
                VolunteerTaskType.valueOf(taskType),
                taskId,
                null,
                title,
                content,
                taskStartTime,
                taskEndTime,
                new Date());
    }

    /**
     * 校验任务类型和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerShiftAddRequest::getTaskType, VolunteerTaskType.class, "request.volunteer.shift.type");
        validateTime(errors, VolunteerShiftAddRequest::getStartTime, VolunteerShiftAddRequest::getEndTime);
        if (taskId == null) { // 创建新任务
            if (taskStartTime == null) errors.rejectValue("taskStartTime", "request.volunteer.shift.time");
            if (taskEndTime == null) errors.rejectValue("taskEndTime", "request.volunteer.shift.time");
            validateTime(errors, VolunteerShiftAddRequest::getTaskStartTime, VolunteerShiftAddRequest::getTaskEndTime);
        }
    }
}
