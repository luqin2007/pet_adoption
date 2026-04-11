package com.example.backend.dto;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.property.AgreementType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class AgreementFilesUploadTable implements ITable {

    @NotEmpty(message = "request.adopt_breading.agreement.file")
    private List<MultipartFile> files;

    public void applyTo(Agreement agreement) {
        agreement.setContent(null);
        agreement.setType(AgreementType.PAPER);
        agreement.setCreateTime(new Date());
    }
}
