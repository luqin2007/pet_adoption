package com.example.backend.dto;

import com.example.backend.entity.MedicalDetail;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class MedicalDetailAddRequest {

    @NotNull(message = "请先创建就诊记录")
    private Long recordId;

    @NotNull(message = "请描述问题")
    @NotEmpty(message = "请描述问题")
    private String description;

    @NotNull(message = "请描述现病史")
    private String history;

    @NotNull(message = "请描述既往史")
    private String pastHistory;

    @NotNull(message = "请描述生活习性")
    private String lifeHabit;

    @NotNull(message = "请输入宠物体重")
    @Min(value = 0, message = "体重不能小于0")
    private Double weight;

    @NotNull(message = "请输入宠物体温")
    @Min(value = 0, message = "体温不能小于0")
    private Double temperature;

    @NotNull(message = "请输入心率")
    @Min(value = 0, message = "心率不能小于0")
    private Double heartRate;

    @NotNull(message = "请输入呼吸频率")
    @Min(value = 0, message = "呼吸频率不能小于0")
    private Double respiratoryRate;

    @NotNull(message = "请输入其他体检信息")
    private String physicalExam;

    public MedicalDetail createEntity(Long userId) {
        return new MedicalDetail(null,
                recordId,
                userId,
                new Date(),
                new Date(),
                false,
                "",
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
