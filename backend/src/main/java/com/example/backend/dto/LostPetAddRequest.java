package com.example.backend.dto;

import com.example.backend.entity.LostPet;
import com.example.backend.entity.property.LostPetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import static com.example.backend.util.StringUtils.normalize;

/**
 * 走失宠物报备请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LostPetAddRequest extends LocationRequest implements IRequest {

    @NotBlank(message = "request.timeout")
    private String uuid;

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

    public LostPet create(Long ownerId) {
        Date now = new Date();
        return new LostPet(null,
                ownerId,
                name,
                age,
                sex,
                normalize(type, true),
                normalize(breed, true),
                features,
                lostTime,
                phone,
                description,
                null,
                LostPetStatus.SEARCHING,
                now,
                now);
    }
}
