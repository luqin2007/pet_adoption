package com.example.backend.dto;

import com.example.backend.entity.PetImage;
import com.example.backend.entity.PetInformation;
import com.example.backend.entity.PetTag;
import com.example.backend.entity.User;
import com.example.backend.util.FileUtils;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
public class PetInfoResponse {

    private Long id;

    /**
     * 宠物名称
     */
    private String name;

    /**
     * 发现者
     */
    private UserResponse user;

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
     * 宠物特征
     */
    private List<PetTagResponse> tags;

    /**
     * 宠物封面地址
     */
    private String cover;

    public static PetInfoResponse fromEntity(PetInformation petInformation, User user, List<PetTag> tags, PetImage cover, FileUtils fileUtils) {
        PetInfoResponse response = new PetInfoResponse();
        response.setId(petInformation.getId());
        response.setName(petInformation.getName());
        response.setUser(UserResponse.fromEntity(user));
        response.setMinAge(petInformation.getMinAge());
        response.setMaxAge(petInformation.getMaxAge());
        response.setSex(petInformation.getSex());
        response.setType(petInformation.getType());
        response.setBreed(petInformation.getBreed());
        response.setHealth(petInformation.getHealth());
        response.setVaccine(petInformation.getVaccine());
        response.setDescription(petInformation.getDescription());
        response.setTags(tags.stream()
                .map(PetTagResponse::fromTag)
                .toList());
        if (cover != null) {
            Path imagePath = fileUtils.buildPetImagePath(petInformation.getId());
            response.setCover(cover.toAssetUrl(imagePath));
        }
        return response;
    }
}
