package com.example.backend.dto.ai;

import com.example.backend.entity.LostPet;
import com.example.backend.entity.Pet;
import lombok.Data;

import java.util.List;

@Data
public class AIMatchResult implements Comparable<AIMatchResult> {
    private LostPet lostPet;
    private Pet pet;
    private Boolean isMatch;
    private Double confidence;
    private List<String> reasons = List.of();

    @Override
    public int compareTo(AIMatchResult other) {
        return other.confidence.compareTo(confidence);
    }
}
