package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.entity.property.PetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Date;

@Data
public class PetStatusUpdateRequest {

    @NotNull
    private Long id;

    @NotNull
    private Long petId;

    @NotNull(message = "错误状态")
    private PetStatus status;

    @NotNull
    private String reason;

    public PetStatusRecord buildStatusRecord(Pet info, Long userId) {
        return new PetStatusRecord(null,
                petId,
                userId,
                info.getStatus(),
                status,
                reason,
                new Date(System.currentTimeMillis()));
    }
}
