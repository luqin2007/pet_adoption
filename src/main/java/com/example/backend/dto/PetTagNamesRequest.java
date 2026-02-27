package com.example.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PetTagNamesRequest {

    @NotEmpty(message = "请输入宠物特征")
    private List<String> tags;
}
