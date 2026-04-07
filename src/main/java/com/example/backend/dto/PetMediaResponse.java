package com.example.backend.dto;

import com.example.backend.entity.MediaFile;
import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class PetMediaResponse implements IResponse {

    private Long id;

    /**
     * 媒体文件类型，IMAGE/VIDEO
     */
    private MediaType type;

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
                media.getType(),
                FileUtils.generateAssetUrl(ParentType.PET, media.getParentId(), media.getFilename()),
                media.getName(),
                media.getDescription(),
                media.getIsCover(),
                media.getCreateTime());
    }
}
