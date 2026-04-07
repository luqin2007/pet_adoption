package com.example.backend.dto;

import com.example.backend.entity.RehabPlan;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Set;

@Data
public class RehabPlanQueryParams implements IParam<RehabPlan>, IRequestValidate {

    private Set<Long> pet;

    private Set<Long> doctor;

    @Override
    public void validate(Errors errors) {
        //noinspection unchecked
        validateOne(errors, RehabPlanQueryParams::getPet, RehabPlanQueryParams::getDoctor);
    }
}
