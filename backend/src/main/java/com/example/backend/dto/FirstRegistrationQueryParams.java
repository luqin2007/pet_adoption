package com.example.backend.dto;

import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class FirstRegistrationQueryParams implements IParam, IValidatedRequest {

    // 筛选
    private Long registrar;
    private Long pet;
    private Long ownerId;
    private Date date0;
    private Date date1;

    // 筛选 (无索引)
    private String name;

    @Override
    public void validate(Errors errors) {
        validateTime(errors, FirstRegistrationQueryParams::getDate0, FirstRegistrationQueryParams::getDate1);
        //noinspection unchecked
        validateOne(errors,
                FirstRegistrationQueryParams::getRegistrar,
                FirstRegistrationQueryParams::getPet,
                FirstRegistrationQueryParams::getOwnerId,
                FirstRegistrationQueryParams::getName);
    }
}
