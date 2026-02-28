package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PetMediaRequest {

    @NotBlank(message = "名称为空")
    private String name;

    @JsonSetter(nulls = Nulls.SKIP)
    private String description = "";

    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean isCover = false;
}
