package com.example.backend.dto;

import com.example.backend.entity.AllergyHistory;
import com.example.backend.entity.FirstRegistration;
import com.example.backend.entity.ImmunityHistory;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class FirstRegistrationAddRequest {

    @NotBlank
    private Long petId;

    @NotBlank
    private Long userId;

    @JsonSetter(nulls = Nulls.SKIP)
    private String name = "";

    @NotBlank(message = "请输入宠物年龄")
    private Integer age;

    @NotNull(message = "请输入疫苗信息")
    private String vaccine;

    @NotBlank(message = "请输入宠物体重")
    private Double weight;

    @NotBlank(message = "请输入宠物体温")
    private Double temperature;

    // 免疫史
    private List<ImmunityHistoryRequest> immunity;

    // 过敏史
    private List<AllergyHistoryAddRequest> allergy;

    private String description;

    public FirstRegistration createEntity(Long userId) {
        return new FirstRegistration(null,
                userId,
                petId,
                name,
                age,
                weight,
                temperature,
                new Date());
    }

    public List<ImmunityHistory> createImmunityHistories(Long registrationId) {
        return immunity.stream().map(request -> request.createEntity(registrationId)).toList();
    }

    public List<AllergyHistory> createAllergyHistories(Long registrationId) {
        return allergy.stream().map(request -> request.build(registrationId)).toList();
    }
}
