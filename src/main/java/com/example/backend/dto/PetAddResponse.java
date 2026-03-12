package com.example.backend.dto;

import com.example.backend.entity.Pet;
import lombok.Data;

@Data
public class PetAddResponse {

    private Long id;

    /**
     * 宠物名称
     */
    private String name;

    /**
     * 宠物最小年龄
     */
    private Integer minAge;

    /**
     * 宠物最大年龄
     */
    private Integer maxAge;

    /**
     * 宠物性别
     */
    private String sex;

    /**
     * 宠物类型
     */
    private String type;

    /**
     * 宠物品种
     */
    private String breed;

    /**
     * 宠物健康情况
     */
    private String health;

    /**
     * 宠物描述
     */
    private String description;

    public static PetAddResponse fromEntity(Pet pet) {
        PetAddResponse response = new PetAddResponse();
        response.setId(pet.getId());
        response.setName(pet.getName());
        response.setMinAge(pet.getMinAge());
        response.setMaxAge(pet.getMaxAge());
        response.setSex(pet.getSex());
        response.setType(pet.getType());
        response.setBreed(pet.getBreed());
        response.setHealth(pet.getHealth());
        response.setDescription(pet.getDescription());
        return response;
    }
}
