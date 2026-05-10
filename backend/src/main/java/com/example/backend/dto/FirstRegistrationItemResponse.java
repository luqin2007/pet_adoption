package com.example.backend.dto;

import com.example.backend.entity.FirstRegistration;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class FirstRegistrationItemResponse implements IResponse {

    private Long id;
    private String name;
    private Integer age;
    private Date createTime;

    // pet
    private Long petId;
    private String sex;
    private String type;
    private String breed;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * FirstRegistration: id, userId, petId, name, age, createTime<br>
     * Pet: id, sex, type, breed<br>
     * User: id, username, avatar
     */
    public static FirstRegistrationItemResponse create(FirstRegistration firstRegistration, Pet pet, User user) {
        return new FirstRegistrationItemResponse(
                firstRegistration.getId(),
                firstRegistration.getName(),
                firstRegistration.getAge(),
                firstRegistration.getCreateTime(),
                pet.getId(),
                pet.getSex(),
                pet.getType(),
                pet.getBreed(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, user.getId(), user.getAvatar()));
    }

    /**
     * FirstRegistration: id, userId, petId, name, age, createTime<br>
     * Pet: id, sex, type, breed<br>
     * User: id, username, avatar<br>
     * <br>
     * pets: FirstRegistration.petId<br>
     * users: FirstRegistration.registrarId
     */
    public static FirstRegistrationItemResponse createBatch(FirstRegistration firstRegistration,
                                                            Map<Long, Pet> pets, Map<Long, User> users) {
        return create(firstRegistration, pets.get(firstRegistration.getPetId()), users.get(firstRegistration.getRegistrarId()));
    }
}
