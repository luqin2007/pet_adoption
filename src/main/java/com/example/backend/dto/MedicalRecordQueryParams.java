package com.example.backend.dto;

import com.example.backend.entity.property.MedicalRecordStatus;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class MedicalRecordQueryParams implements IParam, IValidatedRequest {

    private Long pet;
    private Long doctor;
    private String status;
    private Date time0, time1;

    @Override
    public void validate(Errors errors) {
        //noinspection unchecked
        validateOne(errors, MedicalRecordQueryParams::getPet, MedicalRecordQueryParams::getDoctor);
        validateTime(errors, MedicalRecordQueryParams::getTime0, MedicalRecordQueryParams::getTime1);
        validateEnum(errors, MedicalRecordQueryParams::getStatus, MedicalRecordStatus.class, "request.medical.record.status");
    }
}
