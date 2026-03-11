package com.example.backend.dto;

import com.example.backend.entity.MediaInfo;
import com.example.backend.util.C;
import com.example.backend.util.FileUtils;
import lombok.Data;

import java.util.Date;

@Data
public class PetMediaResponse {

    private Long id;

    /**
     * 媒体文件类型，image/video
     */
    private String type;

    /**
     * 媒体文件路径
     */
    private String path;

    private String name;

    private String description;

    /**
     * 封面，仅图片
     */
    private boolean isCover;

    private Date createDate;

    public static PetMediaResponse fromEntity(MediaInfo media) {
        PetMediaResponse response = new PetMediaResponse();
        response.setType("image");
        response.setPath(FileUtils.generateAssetUrl(C.PARENT_PET, media.getParentId(), media.getFilename()));
        response.setName(media.getName());
        response.setDescription(media.getDescription());
        response.setCover(media.getIsCover());
        response.setCreateDate(media.getCreateTime());
        return response;
    }
}
