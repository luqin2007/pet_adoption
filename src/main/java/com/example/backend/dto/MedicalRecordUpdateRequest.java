package com.example.backend.dto;

import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.property.MedicalRecordStatus;
import com.example.backend.entity.property.MedicalRecordType;
import lombok.Data;

import java.util.Date;

@Data
public class MedicalRecordUpdateRequest {

    private Long ownerId;

    private String ownerPhone;

    private String status;

    private String type;

    private Date startTime;

    private Date endTime;

    private Double cost;

    public void applyTo(MedicalRecord record) {
        record.setOwnerId(ownerId);
        record.setOwnerPhone(ownerPhone);
        record.setStatus(MedicalRecordStatus.get(status));
        record.setType(MedicalRecordType.get(type));
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setCost(cost);
        record.setUpdateTime(new Date());
    }
}
