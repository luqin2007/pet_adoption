package com.example.backend.dto;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskAssign;
import com.example.backend.entity.RescueTaskSuggestion;
import com.example.backend.entity.RescueTaskRecord;
import lombok.Data;

import java.util.List;

@Data
public class RescueTaskResponse {

    /**
     * 任务 id
     */
    private Long id;

    /**
     * 任务简介
     */
    private String summary;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 任务类型
     */
    private Integer type;

    public static RescueTaskResponse fromEntity(RescueTask entity) {
        RescueTaskResponse response = new RescueTaskResponse();
        response.setId(entity.getId());
        response.setSummary(entity.getSummary());
        response.setDescription(entity.getDescription());
        response.setType(entity.getType());
        return response;
    }
}
