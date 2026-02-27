package com.example.backend.dto;

import com.example.backend.entity.PetImage;
import com.example.backend.entity.PetVideo;
import lombok.Data;

import java.util.Date;

@Data
public class PetMediaResponse {

    private Long id;
    private String type;
    private String path;
    private String name;
    private String description;
    private boolean isCover;
    private Date updateDate;

    public static PetMediaResponse fromImage(PetImage image) {
        PetMediaResponse response = new PetMediaResponse();
        response.setType("image");
        response.setPath("/assets/images/pets/" + image.getId());
        response.setName(image.getName());
        response.setDescription(image.getDescription());
        response.setCover(image.getIsCover());
        response.setUpdateDate(image.getCreateTime());
        return response;
    }

    public static PetMediaResponse fromVideo(PetVideo video) {
        PetMediaResponse response = new PetMediaResponse();
        response.setType("video");
        response.setPath("/assets/videos/pets/" + video.getId());
        response.setName(video.getName());
        response.setDescription(video.getDescription());
        response.setUpdateDate(video.getCreateTime());
        return response;
    }
}
