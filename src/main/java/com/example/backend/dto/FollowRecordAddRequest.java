package com.example.backend.dto;

import com.example.backend.entity.FollowRecord;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class FollowRecordAddRequest implements IRequest {

    private Date visitTime;

    @NotBlank(message = "request.adopt_breading.follow_record.summary")
    private String summary;

    @NotBlank(message = "request.adopt_breading.follow_record.life_status")
    private String lifeStatus;

    @NotBlank(message = "request.adopt_breading.follow_record.health_status")
    private String healthStatus;

    @NotBlank(message = "request.adopt_breading.follow_record.risk")
    private String risk;

    @NotBlank(message = "request.adopt_breading.follow_record.suggestion")
    private String suggestion;

    public FollowRecord create(Long taskId, Long volunteerId) {
        return new FollowRecord(null,
                taskId,
                volunteerId,
                summary,
                visitTime == null ? new Date() : visitTime,
                lifeStatus,
                healthStatus,
                risk,
                suggestion,
                new Date());
    }
}
