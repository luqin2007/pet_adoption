package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.property.PetStatus;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import static com.example.backend.util.StringUtils.normalize;

/**
 * 添加流浪宠物请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PetInfoAddRequest extends LocationRequest implements IRequest {

    /**
     * 宠物名称，可由爱心人士起，或留空
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String name = "";

    /**
     * 最小年龄，确定流浪宠物年龄大致范围
     */
    @Min(value = 0, message = "request.pet.age")
    private Integer age;

    /**
     * 性别
     */
    @NotBlank(message = "request.pet.sex")
    private String sex;

    /**
     * 宠物类型
     */
    @NotBlank(message = "request.pet.type")
    private String type;

    /**
     * 宠物品种
     */
    private String breed;

    /**
     * 宠物描述
     */
    private String description;

    /**
     * 宠物健康状况
     */
    private String health;

    public Pet createInfo(Long userId) {
        Date now = new Date();
        return new Pet(null,
                userId,
                name,
                age,
                sex,
                normalize(type, true),
                normalize(breed, true),
                health == null ? "" : health,
                description == null ? "" : description,
                PetStatus.WAITING,
                false,
                now,
                now);
    }
}
