package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import lombok.Data;

@Data
public class RescueTaskResponse {

    private Long id;

    private Long previousId;

    private String summary;

    private String description;

    private Integer type;

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
