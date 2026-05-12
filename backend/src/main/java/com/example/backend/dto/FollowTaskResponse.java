package com.example.backend.dto;

import com.example.backend.entity.FollowRecord;
import com.example.backend.entity.FollowTask;
import com.example.backend.entity.Adopt;
import com.example.backend.entity.Pet;
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

    // adopt
    private Long petId;
    private String petName;
    private Long applicantId;
    private String applicantName;

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
    public static FollowTaskResponse create(FollowTask task, Adopt adopt, Pet pet, User applicant,
                                            User worker, User volunteer, FollowRecord record) {
        return new FollowTaskResponse(
                task.getId(),
                task.getAdoptId(),
                task.getPlanTime(),
                task.getStatus(),
                task.getRemark(),
                task.getCreateTime(),
                task.getUpdateTime(),
                adopt == null ? null : adopt.getPetId(),
                pet == null ? null : pet.getName(),
                adopt == null ? null : adopt.getApplicantId(),
                applicant == null ? null : applicant.getUsername(),
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
     * Adopt: id, petId, applicantId<br>
     * Pet: id, name<br>
     * <br>
     * users: FollowTask.workerId, FollowTask.volunteerId<br>
     * records: FollowTask.id
     */
    public static FollowTaskResponse createBatch(FollowTask task, Map<Long, Adopt> adopts,
                                                 Map<Long, Pet> pets, Map<Long, User> users,
                                                 Map<Long, FollowRecord> records) {
        Adopt adopt = adopts.get(task.getAdoptId());
        return create(task,
                adopt,
                adopt == null ? null : pets.get(adopt.getPetId()),
                adopt == null ? null : users.get(adopt.getApplicantId()),
                users.get(task.getWorkerId()),
                users.get(task.getVolunteerId()),
                records.get(task.getId()));
    }
}
