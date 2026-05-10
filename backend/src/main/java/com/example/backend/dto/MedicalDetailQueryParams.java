package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class MedicalDetailQueryParams {

    List<Long> record;

    List<Long> user;
}
