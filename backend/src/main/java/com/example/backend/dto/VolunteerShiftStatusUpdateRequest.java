package com.example.backend.dto;

import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.VolunteerShiftStatusRecord;
import com.example.backend.entity.property.VolunteerShiftStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerShiftStatusUpdateRequest extends StatusUpdateRequest {

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerShiftStatusUpdateRequest::getStatus, VolunteerShiftStatus.class, "request.volunteer.shift.status");
    }

    public void applyTo(VolunteerShift shift) {
        Date now = new Date();
        VolunteerShiftStatus status = VolunteerShiftStatus.get(this.status);
        shift.setStatus(status);
        shift.setUpdateTime(now);
        if (status == VolunteerShiftStatus.IN_PROGRESS)
            shift.setStartTime(now);
        if (status == VolunteerShiftStatus.COMPLETED)
            shift.setEndTime(now);
    }

    public VolunteerShiftStatusRecord create(VolunteerShift shift) {
        return new VolunteerShiftStatusRecord(null,
                shift.getId(),
                shift.getStatus(),
                VolunteerShiftStatus.get(status),
                reason,
                new Date());
    }
}
