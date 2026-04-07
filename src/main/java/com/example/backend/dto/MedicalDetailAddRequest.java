package com.example.backend.dto;

import com.example.backend.entity.MedicalDetail;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class MedicalDetailAddRequest implements IRequest {

    @NotNull(message = "request.medical.detail.record")
    private Long recordId;

    @NotBlank(message = "request.medical.detail.summary")
    private String summary;

    @NotEmpty(message = "request.medical.detail.description")
    private String description;

    @NotNull(message = "request.medical.detail.history")
    private String history;

    @NotNull(message = "request.medical.detail.past_history")
    private String pastHistory;

    @NotNull(message = "request.medical.detail.life_habit")
    private String lifeHabit;

    @NotNull(message = "request.medical.weight")
    @Min(value = 0, message = "request.medical.weight")
    private Double weight;

    @NotNull(message = "request.medical.temperature")
    @Min(value = 0, message = "request.medical.temperature")
    private Double temperature;

    @NotNull(message = "request.medical.detail.heart")
    @Min(value = 0, message = "request.medical.detail.heart")
    private Integer heartRate;

    @NotNull(message = "request.medical.detail.respiratory")
    @Min(value = 0, message = "request.medical.detail.respiratory")
    private Integer respiratoryRate;

    private String physicalExam;

    public MedicalDetail createEntity(Long userId) {
        return new MedicalDetail(null,
                recordId,
                userId,
                new Date(),
                new Date(),
                false,
                false,
                summary,
                description,
                history,
                pastHistory,
                lifeHabit,
                weight,
                temperature,
                heartRate,
                respiratoryRate,
                physicalExam,
                null,
                null,
                null,
                null,
                null);
    }
}
