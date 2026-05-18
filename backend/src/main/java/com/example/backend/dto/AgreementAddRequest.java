package com.example.backend.dto;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.property.AgreementType;
import com.example.backend.entity.property.ParentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.List;

@Data
public class AgreementAddRequest implements IRequest, IValidatedRequest {

    /**
     * 临时上传批次 id。
     * 纸质协议时用于关联已上传的扫描件。
     */
    private String uuid;

    @NotNull(message = "request.adopt_breading.agreement.parent")
    private Long parentId;

    @NotBlank(message = "request.adopt_breading.agreement.parent")
    private String parentType;

    @NotBlank(message = "request.adopt_breading.agreement.type")
    private String type;

    private String content;

    /**
     * 纸质协议扫描件顺序，列表下标即页码顺序。
     * 元素值为临时上传返回的文件名。
     */
    private List<String> fileOrder;

    public Agreement create(Long applicantId, Long reviewerId) {
        Date now = new Date();
        return new Agreement(null,
                parentId,
                ParentType.get(parentType),
                content,
                AgreementType.get(type),
                applicantId,
                reviewerId,
                null,
                null,
                now,
                now);
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, AgreementAddRequest::getType, AgreementType.class, "request.adopt_breading.agreement.type");
        if (errors.hasFieldErrors("type")) {
            return;
        }

        AgreementType agreementType = AgreementType.get(type);
        if (agreementType == AgreementType.PAPER) {
            if (ObjectUtils.isEmpty(uuid)) {
                errors.rejectValue("id", "request.timeout");
            }
            if (ObjectUtils.isEmpty(fileOrder)) {
                errors.rejectValue("fileOrder", "request.adopt_breading.agreement.file");
            }
        } else if (ObjectUtils.isEmpty(content)) {
            errors.rejectValue("content", "request.adopt_breading.agreement.content");
        }
    }
}
