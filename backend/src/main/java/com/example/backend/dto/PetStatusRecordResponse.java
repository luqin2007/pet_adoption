package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.PetStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class PetStatusRecordResponse implements IResponse {

    // 状态记录
    private String id;
    private PetStatus from;
    private PetStatus to;
    private Date createTime;
    private String description;

    // 对应宠物信息
    private String petId;
    private String petName;
    private String cover;

    // 发起用户信息
    private String userId;
    private String username;
    private String avatar;

    /**
     * Pet: name<br>
     * User: id, username, avatar
     */
    public static PetStatusRecordResponse create(PetStatusRecord record, Pet pet, String cover, User user) {
        return new PetStatusRecordResponse(
                String.valueOf(record.getId()),
                record.getFrom(),
                record.getTo(),
                record.getCreateTime(),
                record.getDescription(),
                String.valueOf(record.getPetId()),
                pet.getName(),
                cover,
                String.valueOf(user.getId()),
                user.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, user.getId(), user.getAvatar()));
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
