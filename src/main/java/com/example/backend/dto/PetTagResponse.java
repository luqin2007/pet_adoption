package com.example.backend.dto;

import com.example.backend.entity.PetTag;
import lombok.Data;

@Data
public class PetTagResponse {

    private Long id;
    private String name;

    public static PetTagResponse fromTag(PetTag petTag) {
        PetTagResponse response = new PetTagResponse();
        response.setId(petTag.getId());
        response.setName(petTag.getTag());
        return response;
    }
}
