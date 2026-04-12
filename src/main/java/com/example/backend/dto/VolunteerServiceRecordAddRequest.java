package com.example.backend.dto;

import com.example.backend.entity.VolunteerServiceRecord;
import com.example.backend.entity.property.VolunteerRecordStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者服务记录创建请求
 */
@Data
public class VolunteerServiceRecordAddRequest implements IRequest, IValidatedRequest {

    /**
     * 服务开始时间
     */
    private Date startTime;
    /**
     * 服务结束时间
     */
    private Date endTime;
    private BigDecimal actualHours;
    /**
     * 服务摘要
     */
    @NotBlank(message = "服务摘要不能为空")
    private String summary;
    private String content;
    private String problem;
    private String suggestion;

    /**
     * 创建服务记录实体
     */
    public VolunteerServiceRecord create(Long shiftId, Long volunteerId) {
        Date now = new Date();
        return new VolunteerServiceRecord(
                null,
                shiftId,
                volunteerId,
                startTime,
                endTime,
                actualHours,
                summary,
                content,
                problem,
                suggestion,
                VolunteerRecordStatus.SUBMITTED,
                null,
                null,
                null,
                now,
                now);
    }

    @Override
    public void validate(Errors errors) {
        validateTime(errors, VolunteerServiceRecordAddRequest::getStartTime, VolunteerServiceRecordAddRequest::getEndTime);
    }
}
