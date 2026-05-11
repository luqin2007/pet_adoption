package com.example.backend.dto;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.property.AgreementType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.AGREEMENT;

@Data
@AllArgsConstructor
public class AgreementResponse implements IResponse {

    private Long id;
    private String petName;
    private Long parentId;
    private ParentType parentType;
    private AgreementType type;
    private String content;
    private String sign;
    private Date signTime;
    private List<AgreementFileResponse> files;
    private Date createTime;
    private Date updateTime;

    public static AgreementResponse create(Agreement agreement, String petName, List<AgreementFileResponse> files) {
        return new AgreementResponse(
                agreement.getId(),
                petName,
                agreement.getParentId(),
                agreement.getParentType(),
                agreement.getType(),
                agreement.getContent(),
                FileUtils.generateAssetUrl(AGREEMENT, agreement.getId(), agreement.getSign()),
                agreement.getSignTime(),
                files,
                agreement.getCreateTime(),
                agreement.getUpdateTime()
        );
    }

    /**
     * files: Agreement.id
     */
    public static AgreementResponse createBatch(Agreement agreement, String petName, Map<Long, List<AgreementFileResponse>> files) {
        return create(agreement, petName, files.getOrDefault(agreement.getId(), List.of()));
    }
}
