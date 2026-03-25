package com.example.backend.dto;

import com.example.backend.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PetAddResponse {

    private Long id;
    private String name;
    private Integer age;
    private String sex;
    private String type;
    private String breed;
    private String health;
    private String description;

    public static PetAddResponse create(Pet pet) {
        return new PetAddResponse(
                pet.getId(),
                pet.getName(),
                pet.getAge(),
                pet.getSex(),
                pet.getType(),
                pet.getBreed(),
                pet.getHealth(),
                pet.getDescription());
    }
}
