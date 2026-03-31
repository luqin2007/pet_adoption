package com.example.backend.dto;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.AgreementUpdateRecord;
import com.example.backend.entity.property.AgreementType;
import com.example.backend.entity.property.AgreementUpdateType;
import com.example.backend.entity.property.ParentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class AgreementAddRequest {

    @NotNull(message = "未知项目")
    private Long parentId;

    @NotBlank(message = "未知项目")
    private String parentType;

    @NotBlank(message = "错误类型")
    private String type;

    @NotBlank(message = "请填写协议内容")
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
}
