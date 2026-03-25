package com.example.backend.dto;

import com.example.backend.entity.*;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;

import static com.example.backend.util.C.PARENT_USER;

@Data
@AllArgsConstructor
public class FirstRegistrationResponse {

    private Long id;
    private String name;
    private Integer age;
    private Double weight;
    private Double temperature;
    private List<ImmunityHistory> immunity; // 免疫史
    private List<AllergyHistory> allergy; // 过敏史
    private Date createTime;
    private List<MedicalDetailItemResponse> results;

    // pet
    private Long petId;
    private String sex;
    private String type;
    private String breed;
    private String cover;

    // user 接诊（记录）人
    private Long userId;
    private String username;
    private String avatar;

    /**
     * Pet: id, sex, type, breed<br>
     * User: id, username, avatar
     */
    public static FirstRegistrationResponse create(FirstRegistration firstRegistration,
                                                   List<ImmunityHistory> immunityHistories,
                                                   List<AllergyHistory> allergyHistories,
                                                   List<MedicalDetailItemResponse> results,
                                                   Pet pet, String cover, User user) {
        return new FirstRegistrationResponse(
                firstRegistration.getId(),
                firstRegistration.getName(),
                firstRegistration.getAge(),
                firstRegistration.getWeight(),
                firstRegistration.getTemperature(),
                immunityHistories,
                allergyHistories,
                firstRegistration.getCreateTime(),
                results,
                pet.getId(),
                pet.getSex(),
                pet.getType(),
                pet.getBreed(),
                cover,
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(PARENT_USER, user.getId(), user.getAvatar()));
    }
}
