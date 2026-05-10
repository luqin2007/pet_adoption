package com.example.backend.dto;

import com.example.backend.entity.PetTag;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PetTagResponse implements IResponse {

    private Long id;
    private String tag;

    /**
     * PetTag: id, tag
     */
    public static PetTagResponse create(PetTag petTag) {
        return new PetTagResponse(petTag.getId(), petTag.getTag());
    }
}
