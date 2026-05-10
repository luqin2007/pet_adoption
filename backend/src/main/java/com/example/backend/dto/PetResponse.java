package com.example.backend.dto;

import com.example.backend.entity.Location;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.query.PetLocations;
import com.example.backend.entity.property.PetStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

@Data
@AllArgsConstructor
public class PetResponse implements IResponse {

    private String id;
    private String name;
    private Integer age;
    private String sex;
    private String type;
    private String breed;
    private String health;
    private String description;
    private PetStatus status;
    private List<PetTagResponse> tags;
    private String cover;
    private List<VaccineResponse> vaccines;
    private List<DewormResponse> deworms;

    // discover
    private String discoverId;
    private String username;
    private String avatar;

    // location
    private List<Location> locations;

    /**
     * User: id, username, avatar
     */
    public static PetResponse create(Pet pet, String coverUrl,
                                     User discover,
                                     List<PetTagResponse> tags,
                                     List<VaccineResponse> vaccines,
                                     List<DewormResponse> deworms,
                                     List<Location> locations) {
        return new PetResponse(
                String.valueOf(pet.getId()),
                pet.getName(),
                pet.getAge(),
                pet.getSex(),
                pet.getType(),
                pet.getBreed(),
                pet.getHealth(),
                pet.getDescription(),
                pet.getStatus(),
                tags,
                coverUrl,
                vaccines,
                deworms,
                String.valueOf(discover.getId()),
                discover.getUsername(),
                FileUtils.generateAssetUrl(USER, discover.getId(), discover.getAvatar()),
                locations);
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: pet.discoverId<br>
     * tags: pet.id<br>
     * covers: pet.id<br>
     * vaccines: pet.id<br>
     * deworms: pet.id<br>
     * locations: pet.id
     */
    public static PetResponse createBatch(PetLocations pet,
                                          Map<Long, User> users,
                                          Map<Long, List<PetTagResponse>> tags,
                                          Map<Long, String> covers,
                                          Map<Long, List<VaccineResponse>> vaccines,
                                          Map<Long, List<DewormResponse>> deworms) {
        return create(pet, covers.get(pet.getId()),
                users.get(pet.getDiscoverId()),
                tags.get(pet.getId()),
                vaccines.get(pet.getId()),
                deworms.get(pet.getId()),
                pet.getLocations());
    }
}
