package com.example.backend.dto;

import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

@Data
public class AdoptQueryParams implements IParam, IValidatedRequest {

    private Set<Long> pet;
    private Set<Long> user;
    private Set<String> status;
    private Date time0;
    private Date time1;

    @Override
    public void validate(Errors errors) {
        validateTime(errors, AdoptQueryParams::getTime0, AdoptQueryParams::getTime1);
    }
}
