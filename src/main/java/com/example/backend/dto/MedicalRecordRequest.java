package com.example.backend.dto;

import com.example.backend.entity.MedicalRecord;
import lombok.Data;

import java.util.Date;

import static com.example.backend.util.C.MEDICAL_VISIT_STATUS_WAITING;

@Data
public class MedicalRecordRequest {

    private Long petId;

    private int petAge;

    private Long userId;

    private Long ownerId;

    private String ownerPhone;

    private Integer type;

    private Date startTime;

    private Double price;

    public MedicalRecord createEntity() {
        Date now = new Date();
        return new MedicalRecord(null,
                petId,
                petAge,
                userId,
                ownerId,
                ownerPhone,
                MEDICAL_VISIT_STATUS_WAITING,
                type,
                startTime,
                null,
                price,
                0.0,
                now,
                now);
    }
}
