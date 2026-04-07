package com.example.backend.dto;

import com.example.backend.entity.FollowRecord;
import com.example.backend.entity.User;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

@Data
@AllArgsConstructor
public class FollowRecordResponse implements IResponse {

    private Long id;
    private Long taskId;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerAvatar;
    private Date visitTime;
    private String lifeStatus;
    private String healthStatus;
    private String risk;
    private String suggestion;
    private Date createTime;

    /**
     * User: id, username, avatar
     */
    public static FollowRecordResponse create(FollowRecord record, User volunteer) {
        return new FollowRecordResponse(
                record.getId(),
                record.getTaskId(),
                record.getVolunteerId(),
                volunteer == null ? null : volunteer.getUsername(),
                volunteer == null ? null : FileUtils.generateAssetUrl(USER, volunteer.getId(), volunteer.getAvatar()),
                record.getVisitTime(),
                record.getLifeStatus(),
                record.getHealthStatus(),
                record.getRisk(),
                record.getSuggestion(),
                record.getCreateTime());
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: FollowRecord.volunteerId
     */
    public static FollowRecordResponse createBatch(FollowRecord record, Map<Long, User> users) {
        return create(record, users.get(record.getVolunteerId()));
    }
}
