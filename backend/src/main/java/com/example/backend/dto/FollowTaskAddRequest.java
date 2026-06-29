package com.example.backend.dto;

import com.example.backend.entity.FollowTask;
import com.example.backend.entity.property.FollowTaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class FollowTaskAddRequest implements IRequest {

    @NotNull(message = "request.adopt_breading.follow_task.volunteer")
    private Long volunteerId;

    @NotNull(message = "request.adopt_breading.follow_task.plan")
    private Date planTime;

    private String remark;

    public FollowTask create(Long adoptId, Long adopterId, Long workerId) {
        Date now = new Date();
        return new FollowTask(null,
                adoptId,
                workerId,
                adopterId,
                volunteerId,
                FollowTaskStatus.CREATE,
                remark,
                planTime,
                now,
                now);
    }
}
