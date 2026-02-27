package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PetMediaRequest {

    @NotBlank(message = "名称为空")
    private String name;

    private String description;

    private Boolean isCover;
}
