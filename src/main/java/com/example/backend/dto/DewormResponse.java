package com.example.backend.dto;

import com.example.backend.entity.*;
import com.example.backend.entity.Dewormer;
import com.example.backend.entity.property.DewormerType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class DewormResponse implements Comparable<DewormResponse> {

    private Long id;
    private Integer times;
    private Date createTime;

    // vaccine
    private Long dewormerId;
    private Long itemId;
    private DewormerType dewormerType;
    private String dewormerName;
    private Integer dewormerTotal;

    // pet
    private Long petId;
    private String petName;
    private String petType;
    private String petSex;
    private String petBreed;
    private Integer petAge;
    private String cover;

    // user
    private Long doctorId;
    private String username;
    private String avatar;

    @Override
    public int compareTo(DewormResponse o) {
        // 默认倒序
        return o.createTime.compareTo(createTime);
    }

    /**
     * Item: id, name<br>
     * Pet: id, name, sex, type, breed, age<br>
     * User: id, username, avatar
     */
    public static DewormResponse create(DewormRecord deworm, Dewormer dewormer, Item dewormerItem, Pet pet, String cover, User doctor) {
        return new DewormResponse(
                deworm.getId(),
                deworm.getTimes(),
                deworm.getCreateTime(),
                dewormer.getId(),
                dewormer.getItemId(),
                dewormer.getType(),
                dewormerItem.getName(),
                dewormer.getTimes(),
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getSex(),
                pet.getBreed(),
                pet.getAge(),
                cover,
                deworm.getDoctorId(),
                doctor.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, doctor.getId(), doctor.getAvatar()));
    }

    /**
     * Item: id, name<br>
     * Pet: id, name, sex, type, breed, age<br>
     * User: id, username, avatar<br>
     * <br>
     * dewormers: DewormRecord.dewormerId<br>
     * dewormerItems: Dewormer.itemId<br>
     * doctors: DewormRecord.doctorId
     */
    public static DewormResponse createBatch(DewormRecord record, Pet pet, String cover,
                                             Map<Long, Dewormer> dewormers,
                                             Map<Long, Item> dewormerItems,
                                             Map<Long, User> doctors) {
        Dewormer dewormer = dewormers.get(record.getDewormerId());
        return create(record, dewormer, dewormerItems.get(dewormer.getItemId()), pet, cover, doctors.get(record.getDoctorId()));
    }

    /**
     * Item: id, name<br>
     * Pet: id, name, sex, type, breed, age<br>
     * User: id, username, avatar<br>
     * <br>
     * pets: DewormRecord.petId<br>
     * covers: DewormRecord.petId<br>
     * dewormers: DewormRecord.dewormerId<br>
     * dewormerItems: Dewormer.itemId<br>
     * doctors: DewormRecord.doctorId
     */
    public static DewormResponse createBatch(DewormRecord record,
                                             Map<Long, Pet> pets, Map<Long, String> covers,
                                             Map<Long, Dewormer> dewormers,
                                             Map<Long, Item> dewormerItems,
                                             Map<Long, User> doctors) {
        Dewormer dewormer = dewormers.get(record.getDewormerId());
        return create(record,
                dewormer, dewormerItems.get(dewormer.getItemId()),
                pets.get(record.getPetId()), covers.get(record.getPetId()),
                doctors.get(record.getDoctorId()));
    }
}
