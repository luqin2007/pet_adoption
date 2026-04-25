package com.example.backend.dto;

import com.example.backend.entity.Location;
import com.example.backend.entity.RescueTask;
import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.entity.property.RescueTaskType;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RescueTaskResponse implements IResponse {

    private Long id;
    private Long previousId;
    private String summary;
    private String description;
    private RescueTaskStatus status;
    private RescueTaskType type;
    private Date createTime;
    private Date updateTime;
    private Location location;
    private List<PetMediaResponse> media;

    public static RescueTaskResponse fromEntity(RescueTask entity) {
        return fromEntity(entity, null, List.of());
    }

    public static RescueTaskResponse fromEntity(RescueTask entity, Location location, List<PetMediaResponse> media) {
        RescueTaskResponse response = new RescueTaskResponse();
        response.setId(entity.getId());
        response.setPreviousId(entity.getPreviousId());
        response.setSummary(entity.getSummary());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setType(entity.getType());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        response.setLocation(location);
        response.setMedia(media == null ? List.of() : media);
        return response;
    }
}
