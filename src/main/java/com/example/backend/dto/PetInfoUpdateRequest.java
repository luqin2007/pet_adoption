package com.example.backend.dto;

import com.example.backend.entity.PetInformation;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PetInfoUpdateRequest {

    /**
     * 宠物名称
     */
    @NotBlank(message = "请输入宠物名称")
    private String name;

    /**
     * 宠物最小年龄
     */
    @Min(value = 0, message = "请输入正确的年龄")
    private Integer minAge;

    /**
     * 宠物最大年龄
     */
    @Min(value = 0, message = "请输入正确的年龄")
    private Integer maxAge;

    /**
     * 宠物性别
     */
    @NotBlank(message = "请选择性别")
    private String sex;

    /**
     * 宠物类型
     */
    @NotBlank(message = "请输入宠物类型")
    private String type;

    /**
     * 宠物品种
     */
    @NotBlank(message = "请输入宠物品种")
    private String breed;

    /**
     * 宠物健康情况
     */
    @NotBlank(message = "请输入宠物健康状态")
    private String health;

    /**
     * 宠物疫苗情况
     */
    @NotBlank(message = "请输入宠物疫苗情况")
    private String vaccine;

    /**
     * 宠物描述
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String description = "";

    /**
     * 宠物状态
     * @see PetInformation#getStatus()
     */
    @Min(value = 0, message = "错误状态")
    @Max(value = 7, message = "错误状态")
    private int status;

    /**
     * 若产生状态变更，此为变更描述
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String statusDesc = "";

    public void apply(PetInformation info) {
        info.setName(name);
        info.setMinAge(minAge);
        info.setMaxAge(maxAge);
        info.setSex(sex);
        info.setType(type);
        info.setBreed(breed);
        info.setHealth(health);
        info.setVaccine(vaccine);
        info.setDescription(description);
        info.setStatus(status);
    }
}
