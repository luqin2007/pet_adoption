package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PetTagIdsRequest {

    @JsonSetter(nulls = Nulls.SKIP)
    private List<Integer> tags = List.of();
}
