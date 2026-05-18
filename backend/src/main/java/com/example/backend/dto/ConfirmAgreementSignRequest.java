package com.example.backend.dto;

import com.example.backend.util.StringUtils;
import lombok.Data;
import org.springframework.validation.Errors;

@Data
public class ConfirmAgreementSignRequest implements IValidatedRequest {

    Boolean agree;

    String reason;

    @Override
    public void validate(Errors errors) {
        if (!Boolean.TRUE.equals(agree) && !StringUtils.hasText(reason)) { // 不同意必须有原因
            errors.rejectValue("reason", "request.reason");
        }
    }
}
