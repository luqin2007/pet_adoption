package com.example.backend.dto;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.AgreementUpdateRecord;
import com.example.backend.entity.property.AgreementUpdateType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class AgreementUpdateRequest implements IRequest {

    @NotBlank(message = "request.adopt_breading.agreement.content")
    private String content;

    public AgreementUpdateRecord applyTo(Agreement agreement) {
        Date now = new Date();
        AgreementUpdateRecord record = new AgreementUpdateRecord(null,
                agreement.getId(),
                agreement.getContent(),
                AgreementUpdateType.UPDATE,
                now);

        agreement.setContent(content);
        agreement.setUpdateTime(now);
        return record;
    }
}
