package com.example.backend.dto;

import com.example.backend.entity.ExaminationFile;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

import static com.example.backend.entity.property.ParentType.EXAMINATION;

@Data
@AllArgsConstructor
public class ExaminationFileResponse implements IResponse {

    private Long id;
    private String name;
    private String assetUrl;
    private Date createTime;

    public static ExaminationFileResponse create(ExaminationFile file) {
        return new ExaminationFileResponse(
                file.getId(),
                file.getFilename(),
                FileUtils.generateAssetUrl(EXAMINATION, file.getExaminationId(), file.getFilename()),
                file.getCreateTime());
    }
}
