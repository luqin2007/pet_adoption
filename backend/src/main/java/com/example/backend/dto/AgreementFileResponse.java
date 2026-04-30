package com.example.backend.dto;

import com.example.backend.entity.AgreementFile;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

import static com.example.backend.entity.property.ParentType.AGREEMENT;

@Data
@AllArgsConstructor
public class AgreementFileResponse implements IResponse {

    private Long id;

    private Long agreementId;

    private String assetUrl;

    private Integer page;

    private Date createTime;

    public static AgreementFileResponse create(AgreementFile file) {
        return new AgreementFileResponse(
                file.getId(),
                file.getAgreementId(),
                FileUtils.generateAssetUrl(AGREEMENT, file.getAgreementId(), file.getFilename()),
                file.getPage(),
                file.getCreateTime());
    }
}
