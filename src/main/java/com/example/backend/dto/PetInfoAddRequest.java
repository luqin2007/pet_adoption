package com.example.backend.dto;

import com.example.backend.entity.PetInformation;
import com.example.backend.entity.PetLocation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Date;
import java.util.List;

/**
 * 添加流浪宠物请求体
 */
@Data
public class PetInfoAddRequest {

    /**
     * 宠物名称，可由爱心人士起，或留空
     */
    private String name;

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
    private String breed;

    /**
     * 宠物描述
     */
    private String description;

    /**
     * 特征信息
     */
    private List<String> tags;

    /**
     * 宠物健康状况
     */
    private String health;

    // 地址信息
    @NotBlank(message = "请输入发现位置")
    private String province;
    @NotBlank(message = "请输入发现位置")
    private String city;
    @NotBlank(message = "请输入发现位置")
    private String county;
    @NotBlank(message = "请输入发现位置")
    private String detailAddress;

    public static PetInformation createInformation(PetInfoAddRequest request) {
        PetInformation petInformation = new PetInformation();
        petInformation.setName(request.getName());
        petInformation.setMinAge(request.getMinAge());
        petInformation.setMaxAge(request.getMaxAge());
        petInformation.setSex(request.getSex());
        petInformation.setType(request.getType());
        petInformation.setBreed(request.getBreed());
        petInformation.setHealth(request.getHealth());
        petInformation.setDescription(request.getDescription());
        petInformation.setStatus(0);
        petInformation.setCreateTime(new Date(System.currentTimeMillis()));
        petInformation.setUpdateTime(new Date(System.currentTimeMillis()));
        return petInformation;
    }

    public static PetLocation createLocation(Long petId, PetInfoAddRequest request) {
        PetLocation petLocation = new PetLocation();
        petLocation.setPetId(petId);
        petLocation.setProvince(request.getProvince());
        petLocation.setCity(request.getCity());
        petLocation.setCounty(request.getCounty());
        petLocation.setDetailAddress(request.getDetailAddress());
        petLocation.setDate(new Date(System.currentTimeMillis()));
        return petLocation;
    }
}
