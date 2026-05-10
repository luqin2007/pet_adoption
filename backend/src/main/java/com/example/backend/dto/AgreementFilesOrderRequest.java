package com.example.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.HashSet;
import java.util.List;

@Data
public class AgreementFilesOrderRequest implements IRequest, IValidatedRequest {

    @NotEmpty(message = "request.adopt_breading.agreement.file_order")
    private List<Long> fileOrder;

    @Override
    public void validate(Errors errors) {
        if (fileOrder == null || fileOrder.isEmpty()) {
            return;
        }
        if (fileOrder.stream().anyMatch(id -> id == null || id <= 0)) {
            errors.rejectValue("fileOrder", "request.adopt_breading.agreement.file_order");
        }
        if (new HashSet<>(fileOrder).size() != fileOrder.size()) {
            errors.rejectValue("fileOrder", "request.adopt_breading.agreement.file_order");
        }
    }
}
