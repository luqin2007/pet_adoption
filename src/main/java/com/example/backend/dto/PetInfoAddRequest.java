package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.util.C;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Date;

import static com.example.backend.util.C.PET_STATUS_WAITING;

/**
 * 添加流浪宠物请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PetInfoAddRequest extends LocationRequest {

    /**
     * 宠物名称，可由爱心人士起，或留空
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String name = "";

    /**
     * 最小年龄，确定流浪宠物年龄大致范围
     */
    @Min(value = 0, message = "请输入正确的年龄")
    private Integer minAge;

    /**
     * 最大年龄，确定流浪宠物年龄大致范围
     */
    @Min(value = 0, message = "请输入正确的年龄")
    private Integer maxAge;

    /**
     * 性别
     */
    @NotBlank(message = "请选择性别")
    private String sex;

    /**
     * 宠物类型 (猫、狗等)
     */
    @NotBlank(message = "请输入宠物类型")
    private String type;

    /**
     * 宠物品种
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String breed = "";

    /**
     * 宠物描述
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String description = "";

    /**
     * 宠物健康状况
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String health = "";

    public Pet createInfo() {
        Pet pet = new Pet();
        pet.setName(getName());
        pet.setMinAge(getMinAge());
        pet.setMaxAge(getMaxAge());
        pet.setSex(getSex());
        pet.setType(getType());
        pet.setBreed(getBreed());
        pet.setHealth(getHealth());
        pet.setDescription(getDescription());
        pet.setStatus(PET_STATUS_WAITING);
        pet.setCreateTime(new Date(System.currentTimeMillis()));
        pet.setUpdateTime(new Date(System.currentTimeMillis()));
        return pet;
    }
}
