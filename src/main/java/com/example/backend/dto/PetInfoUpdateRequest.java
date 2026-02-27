package com.example.backend.dto;

import com.example.backend.entity.PetInformation;
import lombok.Data;

@Data
public class PetInfoUpdateRequest {

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
     * 宠物疫苗情况
     */
    private String vaccine;

    /**
     * 宠物描述
     */
    private String description;

    /**
     * 宠物状态
     * @see PetInformation#getStatus()
     */
    private int status;

    private String statusDesc;
}
