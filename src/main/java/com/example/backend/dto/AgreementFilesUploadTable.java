package com.example.backend.dto;

import com.example.backend.entity.AgreementFile;
import com.example.backend.entity.AgreementUpdateRecord;
import com.example.backend.entity.property.AgreementUpdateType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

@Data
public class AgreementFilesUploadTable {

    private List<MultipartFile> files;

    public AgreementUpdateRecord createUpdateRecord(Long agreementId, List<AgreementFile> agreementFiles, ObjectMapper objectMapper) {
        return new AgreementUpdateRecord(null,
                agreementId,
                objectMapper.writeValueAsString(agreementFiles),
                AgreementUpdateType.UPDATE,
                new Date());
    }
}
