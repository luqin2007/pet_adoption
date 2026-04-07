package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class PetResponse implements IResponse {

    private Long id;
    private String name;
    private Integer age;
    private String sex;
    private String type;
    private String breed;
    private String health;
    private String description;
    private List<PetTagResponse> tags;
    private String cover;
    private List<VaccineResponse> vaccines;
    private List<DewormResponse> deworms;

    // discover
    private Long discoverId;
    private String username;
    private String avatar;

    /**
     * User: id, username, avatar
     */
    public static PetResponse create(Pet pet, String coverUrl,
                                     User discover,
                                     List<PetTagResponse> tags,
                                     List<VaccineResponse> vaccines,
                                     List<DewormResponse> deworms) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getAge(),
                pet.getSex(),
                pet.getType(),
                pet.getBreed(),
                pet.getHealth(),
                pet.getDescription(),
                tags,
                coverUrl,
                vaccines,
                deworms,
                discover.getId(),
                discover.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, discover.getId(), discover.getAvatar()));
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: pet.discoverId<br>
     * tags: pet.id<br>
     * covers: pet.id<br>
     * vaccines: pet.id<br>
     * deworms: pet.id
     */
    public static PetResponse createBatch(Pet pet,
                                          Map<Long, User> users,
                                          Map<Long, List<PetTagResponse>> tags,
                                          Map<Long, String> covers,
                                          Map<Long, List<VaccineResponse>> vaccines,
                                          Map<Long, List<DewormResponse>> deworms) {
        return create(pet, covers.get(pet.getId()),
                users.get(pet.getDiscoverId()),
                tags.get(pet.getId()),
                vaccines.get(pet.getId()),
                deworms.get(pet.getId()));
    }
}
