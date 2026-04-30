package com.example.backend.dto;

import com.example.backend.entity.FollowRecord;
import com.example.backend.entity.FollowTask;
import com.example.backend.entity.User;
import com.example.backend.entity.property.FollowTaskStatus;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class FollowTaskResponse implements IResponse {

    private Long id;
    private Long adoptId;
    private Date planTime;
    private FollowTaskStatus status;
    private String remark;
    private Date createTime;
    private Date updateTime;

    // record
    Long recordId;
    String summary;
    Date visitTime;

    // user
    private Long workerId;
    private String workerName;
    private String workerAvatar;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerAvatar;

    /**
     * User: id, username, avatar<br>
     * FollowRecord: id, summary, visitTime
     */
    public static FollowTaskResponse create(FollowTask task, User worker, User volunteer, FollowRecord record) {
        return new FollowTaskResponse(
                task.getId(),
                task.getAdoptId(),
                task.getPlanTime(),
                task.getStatus(),
                task.getRemark(),
                task.getCreateTime(),
                task.getUpdateTime(),
                record == null ? null : record.getId(),
                record == null ? null : record.getSummary(),
                record == null ? null : record.getVisitTime(),
                task.getWorkerId(),
                worker.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, worker.getId(), worker.getAvatar()),
                task.getVolunteerId(),
                volunteer == null ? null : volunteer.getUsername(),
                volunteer == null ? null : FileUtils.generateAssetUrl(ParentType.USER, volunteer.getId(), volunteer.getAvatar()));
    }

    /**
     * User: id, username, avatar<br>
     * FollowRecord: id, taskId, summary, visitTime<br>
     * <br>
     * users: FollowTask.workerId, FollowTask.volunteerId<br>
     * records: FollowTask.id
     */
    public static FollowTaskResponse createBatch(FollowTask task, Map<Long, User> users, Map<Long, FollowRecord> records) {
        return create(task,
                users.get(task.getWorkerId()),
                users.get(task.getVolunteerId()),
                records.get(task.getId()));
    }
}
