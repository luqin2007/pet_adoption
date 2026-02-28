package com.example.backend.dto;

import com.example.backend.entity.PetImage;
import com.example.backend.entity.PetVideo;
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

    public static PetMediaResponse fromImage(PetImage image, FileUtils fileUtils) {
        PetMediaResponse response = new PetMediaResponse();
        response.setType("image");
        response.setPath(image.toPetImageUrl(fileUtils));
        response.setName(image.getName());
        response.setDescription(image.getDescription());
        response.setCover(image.getIsCover());
        response.setCreateDate(image.getCreateTime());
        return response;
    }

    public static PetMediaResponse fromVideo(PetVideo video, FileUtils fileUtils) {
        PetMediaResponse response = new PetMediaResponse();
        response.setType("video");
        response.setPath(video.toPetVideoUrl(fileUtils));
        response.setName(video.getName());
        response.setDescription(video.getDescription());
        response.setCreateDate(video.getCreateTime());
        return response;
    }
}
