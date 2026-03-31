package com.example.backend.dto;

import com.example.backend.entity.FollowRecord;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class FollowRecordAddRequest {

    private Date visitTime;

    @NotBlank(message = "请输入摘要")
    private String summary;

    @NotBlank(message = "请输入生活状态")
    private String lifeStatus;

    @NotBlank(message = "请输入健康状态")
    private String healthStatus;

    @NotBlank(message = "请输入风险")
    private String risk;

    @NotBlank(message = "请输入建议")
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
