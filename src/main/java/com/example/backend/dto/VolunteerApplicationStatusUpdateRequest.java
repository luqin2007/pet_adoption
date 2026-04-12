package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerApplication;
import com.example.backend.entity.VolunteerProfile;
import com.example.backend.entity.property.VolunteerApplicationStatus;
import com.example.backend.entity.property.VolunteerProfileStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 志愿者申请审核请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerApplicationStatusUpdateRequest extends StatusUpdateRequest {

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerApplicationStatusUpdateRequest::getStatus, VolunteerApplicationStatus.class, "request.volunteer.application.status");
    }

    public void applyTo(VolunteerProfile profile, VolunteerApplication application, User viewer) {
        profile.setStatus(VolunteerProfileStatus.ACTIVE);
        profile.setRealName(application.getRealName());
        profile.setSex(application.getSex());
        profile.setPhone(application.getPhone());
        profile.setProvince(application.getProvince());
        profile.setCity(application.getCity());
        profile.setDistrict(application.getDistrict());
        profile.setAddress(application.getAddress());
        profile.setSkills(application.getSkills());
        profile.setServiceIntention(application.getMotivation());
        profile.setAvailableTimeDesc(application.getAvailableTimeDesc());
        profile.setRemark(application.getExperience());
        profile.setUpdateTime(new Date());
    }

    public VolunteerProfile createProfile(VolunteerApplication application, User volunteer, User viewer) {
        Date now = new Date();
        return new VolunteerProfile(null,
                volunteer.getId(),
                VolunteerProfileStatus.ACTIVE,
                application.getRealName(),
                application.getSex(),
                application.getPhone(),
                application.getProvince(),
                application.getCity(),
                application.getDistrict(),
                application.getAddress(),
                application.getSkills(),
                application.getMotivation(),
                application.getAvailableTimeDesc(),
                application.getExperience(),
                now,
                now);
    }
}
