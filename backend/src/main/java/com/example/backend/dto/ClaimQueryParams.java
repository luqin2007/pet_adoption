package com.example.backend.dto;

import com.example.backend.entity.property.ClaimStatus;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 认领申请查询参数
 */
@Data
public class ClaimQueryParams implements IParam, IValidatedRequest {

    private Set<Long> lostPet;

    private Set<Long> user;

    private Set<String> status;

    private Date time0;

    private Date time1;

    @Override
    public void validate(Errors errors) {
        validateTime(errors, ClaimQueryParams::getTime0, ClaimQueryParams::getTime1);
        validateEnums(errors, ClaimQueryParams::getStatus, ClaimStatus.class, "request.status");
    }
}
