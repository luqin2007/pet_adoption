package com.example.backend.dto;

import com.example.backend.entity.FollowTask;
import com.example.backend.entity.property.FollowTaskStatus;
import lombok.Data;

import java.util.Date;

@Data
public class FollowTaskAddRequest {

    private Long volunteerId;
    private Date planTime;
    private String remark;

    public FollowTask create(Long adoptId, Long workerId) {
        Date now = new Date();
        return new FollowTask(null,
                adoptId,
                workerId,
                volunteerId,
                FollowTaskStatus.PENDING,
                remark,
                planTime,
                now,
                now);
    }
}
