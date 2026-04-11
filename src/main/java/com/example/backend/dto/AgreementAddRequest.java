package com.example.backend.dto;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.property.AgreementType;
import com.example.backend.entity.property.ParentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class AgreementAddRequest implements IRequest, IValidatedRequest {

    @NotNull(message = "request.adopt_breading.agreement.parent")
    private Long parentId;

    @NotBlank(message = "request.adopt_breading.agreement.parent")
    private String parentType;

    @NotBlank(message = "request.adopt_breading.agreement.type")
    private String type;

    private String content;

    private List<MultipartFile> files;

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

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, AgreementAddRequest::getType, AgreementType.class, "request.adopt_breading.agreement.type");
        if (ObjectUtils.isEmpty(content) || ObjectUtils.isEmpty(files))
            errors.rejectValue("content", "request.adopt_breading.agreement.content");
    }
}
