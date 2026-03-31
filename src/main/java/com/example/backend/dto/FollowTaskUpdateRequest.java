package com.example.backend.dto;

import com.example.backend.entity.FollowTask;
import com.example.backend.entity.property.FollowTaskStatus;
import com.example.backend.util.StringUtils;
import lombok.Data;

import java.util.Date;

@Data
public class FollowTaskUpdateRequest {

    private Long volunteerId;
    private Date planTime;
    private String status;
    private String remark;

    public void applyTo(FollowTask task) {
        task.setVolunteerId(volunteerId == null ? task.getVolunteerId() : volunteerId);
        task.setPlanTime(planTime == null ? task.getPlanTime() : planTime);
        task.setRemark(remark == null ? task.getRemark() : remark);
        if (StringUtils.hasText(status)) {
            task.setStatus(FollowTaskStatus.get(status));
        }
        task.setUpdateTime(new Date());
    }
}
