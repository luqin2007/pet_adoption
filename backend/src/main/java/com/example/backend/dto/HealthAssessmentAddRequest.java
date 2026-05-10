package com.example.backend.dto;

import com.example.backend.entity.HealthAssessment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.Date;

@Data
public class HealthAssessmentAddRequest implements IRequest {

    @NotNull(message = "request.pet.age")
    @Min(value = 0, message = "request.pet.age")
    private Integer age;

    @NotNull(message = "request.medical.weight")
    @Min(value = 0, message = "request.medical.weight")
    private Double weight;

    @NotNull(message = "request.medical.assessment.score")
    @Range(min = 0, max = 100, message = "request.medical.assessment.right_score")
    private Integer scoreBcs;

    @NotNull(message = "request.medical.assessment.score")
    @Range(min = 0, max = 100, message = "request.medical.assessment.right_score")
    private Integer scoreMental;

    @NotNull(message = "request.medical.assessment.score")
    @Range(min = 0, max = 100, message = "request.medical.assessment.right_score")
    private Integer scoreAppetite;

    @NotBlank(message = "request.medical.assessment.summary")
    private String summary;

    public HealthAssessment create(Long petId, Long assessorId) {
        return new HealthAssessment(null,
                petId,
                assessorId,
                age,
                weight,
                scoreBcs,
                scoreMental,
                scoreAppetite,
                summary,
                new Date());
    }
}
