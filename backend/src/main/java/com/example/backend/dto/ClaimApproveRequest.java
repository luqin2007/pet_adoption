package com.example.backend.dto;

import com.example.backend.entity.property.ClaimStatus;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClaimApproveRequest extends StatusUpdateRequest implements IValidatedRequest {

    private Long petId;

    @Override
    public void validate(Errors errors) {
        ClaimStatus st;
        try {
            st = ClaimStatus.get(status);
            // 不通过必须有原因
            if (st != ClaimStatus.PASS && !StringUtils.hasText(reason))
                errors.rejectValue("reason", "request.reason");
        } catch (ServiceException e) {
            errors.rejectValue("status", "request.status");
        }
    }
}
