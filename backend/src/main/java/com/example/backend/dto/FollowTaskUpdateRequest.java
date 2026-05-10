package com.example.backend.dto;

import com.example.backend.entity.FollowTask;
import com.example.backend.entity.property.FollowTaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class FollowTaskUpdateRequest implements IRequest, IValidatedRequest {

    @NotNull(message = "request.adopt_breading.follow_task.worker")
    private Long workerId;

    @NotNull(message = "request.adopt_breading.follow_task.volunteer")
    private Long volunteerId;

    @NotNull(message = "request.adopt_breading.follow_task.plan")
    private Date planTime;

    @NotNull(message = "request.adopt_breading.follow_task.status")
    private String status;

    private String remark;

    public void applyTo(FollowTask task) {
        task.setWorkerId(workerId);
        task.setVolunteerId(volunteerId);
        task.setPlanTime(planTime);
        task.setRemark(remark);
        task.setStatus(FollowTaskStatus.get(status));
        task.setUpdateTime(new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, FollowTaskUpdateRequest::getStatus, FollowTaskStatus.class, "request.adopt_breading.follow_task.status");
        validateDependency(errors, FollowTaskUpdateRequest::getPlanTime, remark);
    }
}
