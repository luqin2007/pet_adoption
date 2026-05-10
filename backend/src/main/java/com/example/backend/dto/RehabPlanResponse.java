package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.RehabPlan;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RehabPlanResponse implements IResponse {

    private Long id;
    private String title;
    private String content;
    private String frequency;
    private String status;
    private Date startTime;
    private Date endTime;
    private Date createTime;
    private Integer petAge;
    private List<RehabPlanStatusResponse> statusRecords;

    // user
    private Long doctorId;
    private String username;
    private String avatar;

    // pet
    private Long petId;
    private String petName;
    private String petSex;
    private String petType;
    private String petBreed;
    private String cover;

    /**
     * User: id, username, avatar<br>
     * Pet: id, name, sex, type, breed
     */
    public static RehabPlanResponse create(RehabPlan plan, User doctor, Pet pet, String cover,
                                           List<RehabPlanStatusResponse> statusRecords) {
        return new RehabPlanResponse(
                plan.getId(),
                plan.getTitle(),
                plan.getContent(),
                plan.getFrequency(),
                plan.getStatus().name(),
                plan.getStartTime(),
                plan.getEndTime(),
                plan.getCreateTime(),
                pet.getAge(),
                statusRecords,
                plan.getDoctorId(),
                doctor.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, doctor.getId(), doctor.getAvatar()),
                plan.getPetId(),
                pet.getName(),
                pet.getSex(),
                pet.getType(),
                pet.getBreed(),
                cover);
    }

    /**
     * User: id, username, avatar<br>
     * Pet: id, name, sex, type, breed<br>
     * <br>
     * doctors: plan.doctorId<br>
     * pets: plan.petId<br>
     * covers: plan.petId<br>
     * statusRecords: plan.id
     */
    public static RehabPlanResponse createBatch(RehabPlan plan,
                                                Map<Long, User> doctors,
                                                Map<Long, Pet> pets,
                                                Map<Long, String> covers,
                                                Map<Long, List<RehabPlanStatusResponse>> statusRecords) {
        return create(plan, doctors.get(plan.getDoctorId()),
                pets.get(plan.getPetId()), covers.get(plan.getPetId()),
                statusRecords.get(plan.getId()));
    }
}
