package com.example.backend.dto;

import com.example.backend.entity.FollowRecord;
import com.example.backend.entity.FollowTask;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.Adopt;
import com.example.backend.entity.property.FollowTaskStatus;
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
    private Long adoptId;
    private Long petId;
    private String petName;
    private Long applicantId;
    private String applicantName;
    private FollowTaskStatus taskStatus;
    private Date planTime;
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
    public static FollowRecordResponse create(FollowRecord record,
                                              FollowTask task,
                                              Adopt adopt,
                                              Pet pet,
                                              User volunteer,
                                              User applicant) {
        return new FollowRecordResponse(
                record.getId(),
                record.getTaskId(),
                task == null ? null : task.getAdoptId(),
                adopt == null ? null : adopt.getPetId(),
                pet == null ? null : pet.getName(),
                adopt == null ? null : adopt.getApplicantId(),
                applicant == null ? null : applicant.getUsername(),
                task == null ? null : task.getStatus(),
                task == null ? null : task.getPlanTime(),
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
     * Pet: id, name<br>
     * Adopt: id, petId, applicantId<br>
     * FollowTask: id, adoptId, status, planTime
     */
    public static FollowRecordResponse createBatch(FollowRecord record,
                                                   Map<Long, FollowTask> tasks,
                                                   Map<Long, Adopt> adopts,
                                                   Map<Long, Pet> pets,
                                                   Map<Long, User> users) {
        FollowTask task = tasks.get(record.getTaskId());
        Adopt adopt = task == null ? null : adopts.get(task.getAdoptId());
        Pet pet = adopt == null ? null : pets.get(adopt.getPetId());
        return create(record,
                task,
                adopt,
                pet,
                users.get(record.getVolunteerId()),
                adopt == null ? null : users.get(adopt.getApplicantId()));
    }
}
