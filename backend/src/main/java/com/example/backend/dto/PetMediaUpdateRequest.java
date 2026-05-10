package com.example.backend.dto;

import com.example.backend.entity.MediaFile;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PetMediaUpdateRequest implements IRequest {

    @NotBlank(message = "request.pet.media")
    private String name;

    private String description;

    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean isCover = false;

    public void applyTo(MediaFile media) {
        media.setName(name);
        media.setDescription(description);
        media.setIsCover(isCover);
    }
}
