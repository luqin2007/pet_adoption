package com.example.backend.dto;

import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

@Data
public class BreadingQueryParams implements IParam, IValidatedRequest {

    private Set<Long> applicant;
    private Set<Long> reviewer;
    private Set<String> status;
    private Set<String> petType;
    private Date time0;
    private Date time1;

    @Override
    public void validate(Errors errors) {
        validateTime(errors, BreadingQueryParams::getTime0, BreadingQueryParams::getTime1);
    }
}
