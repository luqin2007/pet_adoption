package com.example.backend.dto;

import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.property.MedicalRecordStatus;
import com.example.backend.entity.property.MedicalRecordType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MedicalRecordUpdateRequest implements IRequest, IRequestValidate {

    private Long ownerId;

    private String ownerPhone;

    @NotBlank(message = "request.medical.record.status")
    private String status;

    @NotBlank(message = "request.medical.record.type")
    private String type;

    private Date startTime;

    private Date endTime;

    private String cost;

    public void applyTo(MedicalRecord record) {
        record.setOwnerId(ownerId);
        record.setOwnerPhone(ownerPhone);
        record.setStatus(MedicalRecordStatus.get(status));
        record.setType(MedicalRecordType.get(type));
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setCost(new BigDecimal(cost));
        record.setUpdateTime(new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, MedicalRecordUpdateRequest::getStatus, MedicalRecordStatus.class, "request.medical.record.status");
        validateEnum(errors, MedicalRecordUpdateRequest::getType, MedicalRecordType.class, "request.medical.record.type");
        validateTime(errors, MedicalRecordUpdateRequest::getStartTime, MedicalRecordUpdateRequest::getEndTime);
        validateNumber(errors, MedicalRecordUpdateRequest::getCost, "request.price");
    }
}
