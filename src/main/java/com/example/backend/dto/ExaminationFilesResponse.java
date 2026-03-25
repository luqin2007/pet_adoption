package com.example.backend.dto;

import com.example.backend.entity.IFile;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExaminationFilesResponse {

    private String name;

    private String assetUrl;

    public static ExaminationFilesResponse create(IFile file, String folder, Long parentId) {
        return new ExaminationFilesResponse(
                file.getFilename(),
                FileUtils.generateAssetUrl(folder, parentId, file.getFilename()));
    }
}
