package com.example.backend.dto;

import com.example.backend.entity.MediaInfo;
import lombok.Data;

import java.util.Date;

@Data
public class TempMediaInfo {
    private Long id;
    private String filename;
    private String name;
    private Long userId;
    private Integer type;
    private Date update;

    public MediaInfo createEntity(Long parentId, String parentType) {
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setParentId(parentId);
        mediaInfo.setParentType(parentType);
        mediaInfo.setUserId(userId);
        mediaInfo.setName(name);
        mediaInfo.setDescription(null);
        mediaInfo.setIsCover(false);
        mediaInfo.setFilename(filename);
        mediaInfo.setType(type);
        mediaInfo.setCreateTime(update);
        return mediaInfo;
    }
}
