package com.example.backend.dto;

import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.property.MedicalRecordStatus;
import com.example.backend.entity.property.MedicalRecordType;
import lombok.Data;

import java.util.Date;

@Data
public class MedicalRecordRequest {

    private int petAge;

    private Long userId;

    private Long ownerId;

    private String ownerPhone;

    private String type;

    private Date startTime;

    private Double price;

    public MedicalRecord createEntity(Long petId) {
        Date now = new Date();
        return new MedicalRecord(null,
                petId,
                petAge,
                userId,
                ownerId,
                ownerPhone,
                MedicalRecordStatus.WAITING,
                MedicalRecordType.get(type),
                startTime,
                null,
                price,
                0.0,
                now,
                now);
    }
}
