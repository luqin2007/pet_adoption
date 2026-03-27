package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.property.RescueTaskType;
import lombok.Data;

@Data
public class RescueTaskResponse {

    private Long id;
    private Long previousId;
    private String summary;
    private String description;
    private RescueTaskType type;

    public static RescueTaskResponse fromEntity(RescueTask entity) {
        RescueTaskResponse response = new RescueTaskResponse();
        response.setId(entity.getId());
        response.setPreviousId(entity.getPreviousId());
        response.setSummary(entity.getSummary());
        response.setDescription(entity.getDescription());
        response.setType(entity.getType());
        return response;
    }
}
