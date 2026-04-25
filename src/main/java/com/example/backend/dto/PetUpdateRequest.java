package com.example.backend.dto;

import com.example.backend.entity.Pet;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

import static com.example.backend.util.StringUtils.normalize;

@Data
public class PetUpdateRequest implements IRequest {

    /**
     * 宠物名称
     */
    @NotBlank(message = "request.pet.name")
    private String name;

    /**
     * 宠物最小年龄
     */
    @Min(value = 0, message = "request.pet.age")
    private Integer age;

    /**
     * 宠物性别
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
    @NotBlank(message = "request.pet.breed")
    private String breed;

    /**
     * 宠物健康情况
     */
    @NotBlank(message = "request.pet.health")
    private String health;

    /**
     * 宠物描述
     */
    private String description;

    public void applyTo(Pet info) {
        info.setName(name);
        info.setAge(age);
        info.setSex(sex);
        info.setType(normalize(type, true));
        info.setBreed(normalize(breed, true));
        info.setHealth(health);
        info.setDescription(description);
        info.setUpdateTime(new Date());
    }
}
