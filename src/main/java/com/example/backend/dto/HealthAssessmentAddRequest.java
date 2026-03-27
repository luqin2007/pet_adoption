package com.example.backend.dto;

import com.example.backend.entity.HealthAssessment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class HealthAssessmentAddRequest {

    @NotNull(message = "请输入年龄")
    @Min(value = 0, message = "请输入正确的年龄")
    private Integer age;

    @NotNull(message = "请输入体重")
    @Min(value = 0, message = "请输入正确的体重")
    private Double weight;

    @NotNull(message = "请输入分数")
    @Min(value = 0, message = "请输入正确的分数")
    private Integer scoreBcs;

    @NotNull(message = "请输入分数")
    @Min(value = 0, message = "请输入正确的分数")
    private Integer scoreMental;

    @NotNull(message = "请输入分数")
    @Min(value = 0, message = "请输入正确的分数")
    private Integer scoreAppetite;

    @NotBlank(message = "请输入评估结果")
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
