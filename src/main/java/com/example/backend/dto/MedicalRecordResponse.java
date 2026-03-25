package com.example.backend.dto;

import com.example.backend.entity.IId;
import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.util.C.PARENT_USER;

/**
 * 初诊登记
 */
@Data
@AllArgsConstructor
public class MedicalRecordResponse implements IId {

    private Long id;
    private Integer status;
    private Integer type;
    private Date startTime;
    private Date endTime;
    private Double price;
    private Double cost;
    private String ownerPhone;
    private Date createTime;
    private Date updateTime;

    // pet
    private Long petId;
    private String petName;
    private String petSex;
    private String petType;
    private String petBreed;
    private String cover;

    // user
    private Long userId;
    private String username;
    private String avatar;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;

    /**
     * Pet: id, name, sex, type, breed<br>
     * User: id, username, avatar<br>
     */
    public static MedicalRecordResponse create(MedicalRecord record, Pet pet, String cover, User user, User owner) {
        return new MedicalRecordResponse(record.getId(),
                record.getStatus(),
                record.getType(),
                record.getStartTime(),
                record.getEndTime(),
                record.getPrice(),
                record.getCost(),
                record.getOwnerPhone(),
                record.getCreateTime(),
                record.getUpdateTime(),
                pet.getId(), pet.getName(), pet.getSex(), pet.getType(), pet.getBreed(), cover,
                user.getId(), user.getUsername(), FileUtils.generateAssetUrl(PARENT_USER, user.getId(), user.getAvatar()),
                record.getOwnerId(),
                owner == null ? null : owner.getUsername(),
                owner == null ? null : FileUtils.generateAssetUrl(PARENT_USER, owner.getId(), owner.getAvatar()));
    }

    /**
     * Pet: id, name, sex, type, breed<br>
     * User: id, username, avatar<br>
     * <br>
     * users: MedicalRecord.userId, MedicalRecord.ownerId
     */
    public static MedicalRecordResponse createBatch(MedicalRecord record,
                                                    Map<Long, Pet> pets,
                                                    Map<Long, String> covers,
                                                    Map<Long, User> users) {
        return create(record,
                pets.get(record.getPetId()), covers.get(record.getPetId()),
                users.get(record.getDoctorId()),
                users.get(record.getOwnerId()));
    }
}
