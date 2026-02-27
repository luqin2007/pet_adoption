package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PetTagIdsRequest {

    private List<Integer> tags;
}
