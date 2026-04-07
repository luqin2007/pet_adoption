package com.example.backend.dto;

import com.example.backend.entity.RescueTaskRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.RescueTaskAction;
import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class RescueTaskRecordResponse implements IResponse {

    private Long id;
    private Long taskId;
    private RescueTaskAction action;
    /**
     * 修改前的任务状态
     */
    private RescueTaskStatus statusFrom;
    /**
     * 修改后的任务状态
     */
    private RescueTaskStatus statusTo;
    /**
     * 修改原因
     */
    private String reason;
    private Date createTime;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * User: id, username, avatar
     */
    public static RescueTaskRecordResponse create(RescueTaskRecord record, User user) {
        return new RescueTaskRecordResponse(
                record.getId(),
                record.getTaskId(),
                record.getAction(),
                record.getStatusFrom(),
                record.getStatusTo(),
                record.getReason(),
                record.getCreateTime(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, user.getId(), user.getAvatar()));
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: RescueTaskRecord.userId
     */
    public static RescueTaskRecordResponse createBatch(RescueTaskRecord record, Map<Long, User> users) {
        return create(record, users.get(record.getUserId()));
    }
}
