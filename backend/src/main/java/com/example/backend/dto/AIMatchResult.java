package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIMatchResult {
    private Long lostPetId;
    private Long petId;
    private Boolean isMatch;
    private Double confidence;
    private List<String> reasons = List.of();
}
