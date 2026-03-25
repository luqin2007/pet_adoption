package com.example.backend.dto;

import com.example.backend.entity.MedicalRecord;
import lombok.Data;

import java.util.Date;

@Data
public class MedicalRecordUpdateRequest {

    private Long ownerId;

    private String ownerPhone;

    private Integer status;

    private Integer type;

    private Date startTime;

    private Date endTime;

    private Double cost;

    public void applyTo(MedicalRecord record) {
        record.setOwnerId(ownerId);
        record.setOwnerPhone(ownerPhone);
        record.setStatus(status);
        record.setType(type);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setCost(cost);
        record.setUpdateTime(new Date());
    }
}
