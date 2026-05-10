package com.example.backend.dto;

import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Set;

@Data
public class RehabPlanQueryParams implements IParam, IValidatedRequest {

    private Set<Long> pet;

    private Set<Long> doctor;

    @Override
    public void validate(Errors errors) {
        // 空条件用于后台康复计划总览；指定条件时仍保持 pet 与 doctor 互斥。
        validateOne(errors, RehabPlanQueryParams::getPet, RehabPlanQueryParams::getDoctor);
    }
}
