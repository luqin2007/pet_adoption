package com.example.backend.dto;

import com.example.backend.entity.PetInformation;
import lombok.Data;

@Data
public class PetInfoAddResponse {

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

    public static PetInfoAddResponse fromEntity(PetInformation petInformation) {
        PetInfoAddResponse response = new PetInfoAddResponse();
        response.setId(petInformation.getId());
        response.setName(petInformation.getName());
        response.setMinAge(petInformation.getMinAge());
        response.setMaxAge(petInformation.getMaxAge());
        response.setSex(petInformation.getSex());
        response.setType(petInformation.getType());
        response.setBreed(petInformation.getBreed());
        response.setHealth(petInformation.getHealth());
        response.setDescription(petInformation.getDescription());
        return response;
    }
}
