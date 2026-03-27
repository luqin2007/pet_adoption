package com.example.backend.dto;

import com.example.backend.entity.HealthAssessment;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class HealthAssessmentResponse {

    private Long id;
    private Integer scoreBcs;
    private Integer scoreMental;
    private Integer scoreAppetite;
    private Double weight;
    private String summary;
    private Date createTime;

    // pet
    private Long petId;
    private String petName;
    private String petType;
    private String petSex;
    private String petBreed;
    private Integer petAge;
    private String cover;

    // user
    private Long assessorId;
    private String username;
    private String avatar;

    /**
     * Pet: id, name, sex, type, breed<br>
     * User: id, username, avatar
     */
    public static HealthAssessmentResponse create(HealthAssessment assessment, Pet pet, String cover, User assessor) {
        return new HealthAssessmentResponse(
                assessment.getId(),
                assessment.getScoreBcs(),
                assessment.getScoreMental(),
                assessment.getScoreAppetite(),
                assessment.getWeight(),
                assessment.getSummary(),
                assessment.getCreateTime(),
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getSex(),
                pet.getBreed(),
                assessment.getAge(),
                cover,
                assessor.getId(),
                assessor.getUsername(),
                assessor.getAvatar());
    }

    /**
     * Pet: id, name, sex, type, breed<br>
     * User: id, username, avatar<br>
     * <br>
     * pets: HealthAssessment.petId<br>
     * covers: HealthAssessment.petId<br>
     * assessors: HealthAssessment.assessorId
     */
    public static HealthAssessmentResponse createBatch(HealthAssessment assessment,
                                                       Map<Long, Pet> pets,
                                                       Map<Long, String> covers,
                                                       Map<Long, User> assessors) {
        return create(assessment,
                pets.get(assessment.getPetId()),
                covers.get(assessment.getPetId()),
                assessors.get(assessment.getAssessorId()));
    }
}
