package com.example.backend.dto;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.AgreementUpdateRecord;
import com.example.backend.entity.property.AgreementType;
import com.example.backend.entity.property.AgreementUpdateType;
import com.example.backend.entity.property.ParentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class AgreementAddRequest implements IRequest, IValidatedRequest {

    @NotNull(message = "request.adopt_breading.agreement.parent")
    private Long parentId;

    @NotBlank(message = "request.adopt_breading.agreement.parent")
    private String parentType;

    @NotBlank(message = "request.adopt_breading.agreement.type")
    private String type;

    @NotBlank(message = "request.adopt_breading.agreement.content")
    private String content;

    public Agreement create() {
        Date now = new Date();
        return new Agreement(null,
                parentId,
                ParentType.get(parentType),
                content,
                AgreementType.get(type),
                null,
                null,
                now,
                now);
    }

    public AgreementUpdateRecord createUpdateRecord(Agreement agreement) {
        return new AgreementUpdateRecord(null,
                agreement.getId(),
                "",
                AgreementUpdateType.CREATE,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, AgreementAddRequest::getType, AgreementType.class, "request.adopt_breading.agreement.type");
    }
}
