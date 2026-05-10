package com.example.backend.dto;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.property.AgreementType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class AgreementUpdateRequest implements IRequest {

    @NotBlank(message = "request.adopt_breading.agreement.content")
    private String content;

    public void applyTo(Agreement agreement) {
        agreement.setContent(content);
        agreement.setType(AgreementType.ELECTRONIC);
        agreement.setUpdateTime(new Date());
    }
}
