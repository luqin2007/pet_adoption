package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RehabPlanQueryParams {

    private List<Long> pet;

    private List<Long> user;
}
