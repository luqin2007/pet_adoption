package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerServiceRecord;
import com.example.backend.entity.property.VolunteerRecordStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 志愿者服务记录审核请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerRecordReviewRequest extends StatusUpdateRequest {

    public void applyTo(VolunteerServiceRecord record, User user) {
        VolunteerRecordStatus st = VolunteerRecordStatus.get(getStatus());
        record.setStatus(st);
        if (st.isReview()) {
            record.setReviewerId(user.getId());
            record.setReviewComment(getReason());
            record.setReviewTime(new Date());
        }
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerRecordReviewRequest::getStatus, VolunteerRecordStatus.class, "request.volunteer.record.status");
    }
}
