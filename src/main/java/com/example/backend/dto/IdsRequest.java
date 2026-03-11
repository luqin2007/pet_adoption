package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.util.List;

@Data
public class IdsRequest {

    @JsonSetter(nulls = Nulls.SKIP)
    private List<Long> ids = List.of();
}
