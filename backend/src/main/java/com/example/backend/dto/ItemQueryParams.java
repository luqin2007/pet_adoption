package com.example.backend.dto;

import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;

import java.util.Set;

@Data
public class ItemQueryParams implements IParam, IValidatedRequest {

    private Set<Long> category;

    private String keyword;

    @Override
    public void validate(Errors errors) {
        if (ObjectUtils.isEmpty(category) && ObjectUtils.isEmpty(keyword)) {
            errors.rejectValue("category", "request.query");
        }
    }
}
