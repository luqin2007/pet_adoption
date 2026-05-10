package com.example.backend.dto;

import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

@Data
public class DonationQueryParams implements IParam, IValidatedRequest {

    private Set<Long> user;

    private Date date0;

    private Date date1;

    @Override
    public void validate(Errors errors) {
        validateTime(errors, DonationQueryParams::getDate0, DonationQueryParams::getDate1);
    }
}
