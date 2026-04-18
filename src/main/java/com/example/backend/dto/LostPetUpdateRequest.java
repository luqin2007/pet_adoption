package com.example.backend.dto;

import com.example.backend.entity.LostPet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 更新走失宠物报备请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LostPetUpdateRequest extends LocationRequest implements IRequest {

    @NotBlank(message = "request.pet.name")
    private String name;

    @NotNull(message = "request.pet.age")
    private Integer age;

    @NotBlank(message = "request.pet.sex")
    private String sex;

    @NotBlank(message = "request.lost_pet.type")
    private String type;

    private String breed;

    private String features;

    @NotNull(message = "request.lost_pet.time")
    private Date lostTime;

    @NotBlank(message = "request.phone")
    private String phone;

    private String description;

    public void applyTo(LostPet lostPet) {
        lostPet.setName(name);
        lostPet.setAge(age);
        lostPet.setSex(sex);
        lostPet.setType(type);
        lostPet.setBreed(breed);
        lostPet.setFeatures(features);
        lostPet.setLostTime(lostTime);
        lostPet.setPhone(phone);
        lostPet.setDescription(description);
        lostPet.setUpdateTime(new Date());
    }
}
