package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RehabPlanQueryRequest {

    private List<Long> pet;

    private List<Long> user;
}
