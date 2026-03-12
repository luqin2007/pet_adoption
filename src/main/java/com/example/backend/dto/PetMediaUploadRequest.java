package com.example.backend.dto;

import com.example.backend.entity.MediaInfo;
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

import static com.example.backend.util.C.PARENT_PET;

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

    /**
     * 从上传文件生成媒体信息
     *
     * @param parentId   与之关联的资源 id
     * @param userId     上传用户 id
     * @param file       上传文件
     */
    public MediaInfo createMedia(Long parentId, Long userId, MultipartFile file) {
        String oriName = file.getOriginalFilename();
        String fileName = FileUtils.getNameWithoutExtension(oriName);
        Pair<String, Integer> extAndType = FileUtils.getFileExtensionAndType(file);

        Date now = new Date();
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setParentId(parentId);
        mediaInfo.setParentType(PARENT_PET);
        mediaInfo.setUserId(userId);
        mediaInfo.setName(StringUtils.hasText(name) ? name : fileName);
        mediaInfo.setDescription(description);
        mediaInfo.setIsCover(false);
        mediaInfo.setFilename(FileUtils.generateFilename(fileName, now, extAndType.getFirst()));
        mediaInfo.setType(extAndType.getSecond());
        mediaInfo.setCreateTime(now);
        return mediaInfo;
    }
}
