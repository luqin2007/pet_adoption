package com.example.backend.dto;

import com.example.backend.entity.property.ParentType;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class AgreementQueryParams implements IParam, IValidatedRequest {

    private Long parentId;
    private String parentType;
    private Boolean signed;
    private Date time0;
    private Date time1;

    @Override
    public void validate(Errors errors) {
        validateTime(errors, AgreementQueryParams::getTime0, AgreementQueryParams::getTime1);
        validateDependency(errors, AgreementQueryParams::getParentId, parentType);
        validateEnum(errors, AgreementQueryParams::getParentType, ParentType.class, "request.adopt_breading.agreement.type");
    }
}
