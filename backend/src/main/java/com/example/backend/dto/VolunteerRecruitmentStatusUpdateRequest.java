package com.example.backend.dto;

import com.example.backend.entity.property.VolunteerRecruitmentStatus;
import org.springframework.validation.Errors;

public class VolunteerRecruitmentStatusUpdateRequest extends StatusUpdateRequest {

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerRecruitmentStatusUpdateRequest::getStatus, VolunteerRecruitmentStatus.class, "request.volunteer.recruitment.status");
    }
}
