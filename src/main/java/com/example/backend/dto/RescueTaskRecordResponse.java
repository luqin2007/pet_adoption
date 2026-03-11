package com.example.backend.dto;

import com.example.backend.entity.RescueTaskRecord;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class RescueTaskRecordResponse {

    Long id;

    Long taskId;

    UsernameAndAvatarResponse user;

    Integer action;

    /**
     * 修改前的任务状态
     */
    Integer statusFrom;

    /**
     * 修改后的任务状态
     */
    Integer statusTo;

    /**
     * 修改原因
     */
    String reason;

    Date createTime;

    public static RescueTaskRecordResponse fromEntity(RescueTaskRecord entity, Map<Long, UsernameAndAvatarResponse> users) {
        RescueTaskRecordResponse response = new RescueTaskRecordResponse();
        response.setId(entity.getId());
        response.setTaskId(entity.getTaskId());
        response.setUser(users.get(entity.getUserId()));
        response.setAction(entity.getAction());
        response.setStatusFrom(entity.getStatusFrom());
        response.setStatusTo(entity.getStatusTo());
        response.setReason(entity.getReason());
        response.setCreateTime(entity.getCreateTime());
        return response;
    }
}
