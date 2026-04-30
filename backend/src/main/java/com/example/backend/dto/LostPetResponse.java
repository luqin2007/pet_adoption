package com.example.backend.dto;

import com.example.backend.entity.Location;
import com.example.backend.entity.LostPet;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.property.LostPetStatus;
import com.example.backend.entity.query.LostPetLocation;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.PET;
import static com.example.backend.entity.property.ParentType.USER;

/**
 * 走失宠物报备响应
 */
@Data
@AllArgsConstructor
public class LostPetResponse implements IResponse {

    private Long id;
    private String name;
    private Integer age;
    private String sex;
    private String type;
    private String breed;
    private String features;
    private Date lostTime;
    private String contactPhone;
    private String description;
    private LostPetStatus status;
    private Date createTime;
    private Date updateTime;

    // location
    private Location location;

    // owner
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;

    // matched pet info
    private Long petId;
    private String petName;
    private String petCover;
    private List<PetResponse> pets;

    /**
     * User: id, username, avatar<br>
     * Pet: id, name
     */
    public static LostPetResponse create(LostPet lostPet, Location location, User owner,
                                         Pet pet, String cover,
                                         List<PetResponse> pets) {
        return new LostPetResponse(
                lostPet.getId(),
                lostPet.getName(),
                lostPet.getAge(),
                lostPet.getSex(),
                lostPet.getType(),
                lostPet.getBreed(),
                lostPet.getFeatures(),
                lostPet.getLostTime(),
                lostPet.getPhone(),
                lostPet.getDescription(),
                lostPet.getStatus(),
                lostPet.getCreateTime(),
                lostPet.getUpdateTime(),
                location,
                lostPet.getOwnerId(),
                owner.getUsername(),
                FileUtils.generateAssetUrl(USER, owner.getId(), owner.getAvatar()),
                lostPet.getPetId(),
                pet == null ? null : pet.getName(),
                pet == null ? null : FileUtils.generateAssetUrl(PET, pet.getId(), cover),
                pets);
    }

    /**
     * User: id, username, avatar<br>
     * Pet: id, name<br>
     * <br>
     * locations: LostPet.id / null<br>
     * owners: LostPet.ownerId<br>
     * pets: LostPet.petId<br>
     * covers: LostPet.petId
     */
    public static LostPetResponse createBatch(LostPet lostPet,
                                              Map<Long, Location> locations,
                                              Map<Long, User> owners,
                                              Map<Long, Pet> pets,
                                              Map<Long, String> petCovers) {
        Long petId = lostPet.getPetId();
        return create(lostPet,
                lostPet instanceof LostPetLocation l ? l.getLocation() : locations.get(lostPet.getId()),
                owners.get(lostPet.getOwnerId()),
                petId == null ? null : pets.get(petId),
                petId == null ? null : petCovers.get(petId),
                List.of());
    }
}
