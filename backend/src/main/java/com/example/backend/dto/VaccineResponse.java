package com.example.backend.dto;

import com.example.backend.entity.*;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class VaccineResponse implements Comparable<VaccineResponse>, IResponse {

    private Long id;
    private Integer times;
    private Date createTime;

    // vaccine
    private Long vaccineId;
    private Long itemId;
    private String vaccineName;
    private String vaccineIll;
    private Integer vaccineTotal;

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
    public int compareTo(VaccineResponse o) {
        // 默认倒序
        return o.createTime.compareTo(createTime);
    }

    /**
     * Item: id, name<br>
     * Pet: id, name, sex, type, breed, age<br>
     * User: id, username, avatar
     */
    public static VaccineResponse create(VaccineRecord record, Vaccine vaccine, Item vaccineItem, Pet pet, String cover, User doctor) {
        return new VaccineResponse(
                record.getId(),
                record.getTimes(),
                record.getCreateTime(),
                vaccine.getId(),
                vaccine.getItemId(),
                vaccineItem.getName(),
                vaccine.getIllness(),
                vaccine.getTimes(),
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getSex(),
                pet.getBreed(),
                pet.getAge(),
                cover,
                record.getDoctorId(),
                doctor.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, doctor.getId(), doctor.getAvatar()));
    }

    /**
     * Item: id, name<br>
     * Pet: id, name, sex, type, breed, age<br>
     * User: id, username, avatar<br>
     * <br>
     * vaccines: VaccineRecord.vaccineId<br>
     * vaccineItems: Vaccine.itemId<br>
     * doctors: VaccineRecord.doctorId
     */
    public static VaccineResponse createBatch(VaccineRecord record, Pet pet, String cover,
                                              Map<Long, Vaccine> vaccines,
                                              Map<Long, Item> vaccineItems,
                                              Map<Long, User> doctors) {
        Vaccine vaccine = vaccines.get(record.getVaccineId());
        return create(record, vaccine, vaccineItems.get(vaccine.getItemId()), pet, cover, doctors.get(record.getDoctorId()));
    }

    /**
     * Item: id, name<br>
     * Pet: id, name, sex, type, breed, age<br>
     * User: id, username, avatar<br>
     * <br>
     * pets: DewormRecord.petId<br>
     * covers: DewormRecord.petId<br>
     * vaccines: VaccineRecord.vaccineId<br>
     * vaccineItems: Vaccine.itemId<br>
     * doctors: VaccineRecord.doctorId
     */
    public static VaccineResponse createBatch(VaccineRecord record,
                                              Map<Long, Pet> pets, Map<Long, String> covers,
                                              Map<Long, Vaccine> vaccines,
                                              Map<Long, Item> vaccineItems,
                                              Map<Long, User> doctors) {
        Vaccine vaccine = vaccines.get(record.getVaccineId());
        return create(record,
                vaccine, vaccineItems.get(vaccine.getItemId()),
                pets.get(record.getPetId()), covers.get(record.getPetId()),
                doctors.get(record.getDoctorId()));
    }
}
