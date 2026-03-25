package com.example.backend.dto;

import com.example.backend.entity.MediaFile;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

import static com.example.backend.util.C.MEDIA_TYPE_IMAGE;
import static com.example.backend.util.C.PARENT_PET;

@Data
@AllArgsConstructor
public class PetMediaResponse {

    private Long id;

    /**
     * 媒体文件类型，image/video
     */
    private String type;

    /**
     * 媒体文件路径
     */
    private String assetUrl;

    private String name;

    private String description;

    /**
     * 封面，仅图片
     */
    private Boolean isCover;

    private Date createDate;

    public static PetMediaResponse create(MediaFile media) {
        return new PetMediaResponse(
                media.getId(),
                MEDIA_TYPE_IMAGE.equals(media.getType()) ? "image" : "video",
                FileUtils.generateAssetUrl(PARENT_PET, media.getParentId(), media.getFilename()),
                media.getName(),
                media.getDescription(),
                media.getIsCover(),
                media.getCreateTime());
    }
}
