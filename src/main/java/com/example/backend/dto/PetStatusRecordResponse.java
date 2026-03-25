package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.entity.User;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.util.C.PARENT_USER;

@Data
@AllArgsConstructor
public class PetStatusRecordResponse {

    // 状态记录
    Long id;
    int from;
    int to;
    Date createTime;
    String description;

    // 对应宠物信息
    Long petId;
    String petName;
    String cover;

    // 发起用户信息
    Long userId;
    String username;
    String avatar;

    /**
     * Pet: name<br>
     * User: id, username, avatar
     */
    public static PetStatusRecordResponse create(PetStatusRecord record, Pet pet, String cover, User user) {
        return new PetStatusRecordResponse(
                record.getId(),
                record.getFrom(),
                record.getTo(),
                record.getCreateTime(),
                record.getDescription(),
                record.getPetId(),
                pet.getName(),
                cover,
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(PARENT_USER, user.getId(), user.getAvatar()));
    }

    /**
     * Pet: id, name<br>
     * User: id, username, avatar<br>
     * <br>
     * pets: PetStatusRecord.petId<br>
     * covers: PetStatusRecord.petId<br>
     * users: PetStatusRecord.userId
     */
    public static PetStatusRecordResponse createBatch(PetStatusRecord record,
                                                      Map<Long, Pet> pets,
                                                      Map<Long, String> covers,
                                                      Map<Long, User> users) {
        return create(record, pets.get(record.getPetId()), covers.get(record.getPetId()), users.get(record.getUserId()));
    }
}
