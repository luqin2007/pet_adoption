package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AgreementFilesUploadTable implements ITable, IValidatedRequest {

    @NotNull(message = "request.adopt_breading.agreement.file")
    private MultipartFile file;

    @NotNull(message = "request.adopt_breading.agreement.page")
    private Integer page;

    @Override
    public void validate(Errors errors) {
        if (page != null && page <= 0) {
            errors.rejectValue("page", "request.adopt_breading.agreement.page");
        }
    }
}
