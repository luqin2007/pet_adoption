package com.example.backend.dto;

import com.example.backend.entity.ExaminationFile;
import com.example.backend.util.C;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ExaminationFileResponse {

    private Long id;
    private String name;
    private String assetUrl;
    private Date createTime;

    public static ExaminationFileResponse create(ExaminationFile file) {
        return new ExaminationFileResponse(
                file.getId(),
                file.getFilename(),
                FileUtils.generateAssetUrl(C.PARENT_EXAMINATION, file.getExaminationId(), file.getFilename()),
                file.getCreateTime());
    }
}
