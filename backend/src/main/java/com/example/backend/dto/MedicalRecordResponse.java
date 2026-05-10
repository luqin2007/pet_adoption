package com.example.backend.dto;

import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.property.MedicalRecordStatus;
import com.example.backend.entity.property.MedicalRecordType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 初诊登记
 */
@Data
@AllArgsConstructor
public class MedicalRecordResponse implements IResponse {

    private Long id;
    private MedicalRecordStatus status;
    private MedicalRecordType type;
    private Date startTime;
    private Date endTime;
    private String price;
    private String cost;
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
                String.valueOf(record.getPrice()),
                String.valueOf(record.getCost()),
                record.getOwnerPhone(),
                record.getCreateTime(),
                record.getUpdateTime(),
                pet.getId(), pet.getName(), pet.getSex(), pet.getType(), pet.getBreed(), cover,
                user.getId(), user.getUsername(), FileUtils.generateAssetUrl(ParentType.USER, user.getId(), user.getAvatar()),
                record.getOwnerId(),
                owner == null ? null : owner.getUsername(),
                owner == null ? null : FileUtils.generateAssetUrl(ParentType.USER, owner.getId(), owner.getAvatar()));
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
