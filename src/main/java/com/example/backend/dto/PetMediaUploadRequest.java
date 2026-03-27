package com.example.backend.dto;

import com.example.backend.entity.MediaFile;
import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import com.example.backend.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
public class PetMediaUploadRequest {

    @NotBlank(message = "名称为空")
    private String name;

    @JsonSetter(nulls = Nulls.SKIP)
    private String description = "";

    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean isCover = false;

    @NotEmpty(message = "文件为空")
    private MultipartFile file;

    public MediaFile createMedia(Long parentId, Long userId, MultipartFile file) {
        String oriName = file.getOriginalFilename();
        String fileName = FileUtils.getNameWithoutExtension(oriName);
        Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
        Date now = new Date();
        return new MediaFile(null,
                parentId,
                ParentType.PET,
                userId,
                StringUtils.hasText(name) ? name : fileName,
                description,
                isCover,
                FileUtils.generateFilename(fileName, now, extAndType.getFirst()),
                extAndType.getSecond(),
                now);
    }
}
