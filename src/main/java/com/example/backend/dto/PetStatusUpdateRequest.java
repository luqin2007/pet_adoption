package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.util.C;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.sql.Date;

import static com.example.backend.util.C.PET_STATUS_MAX;
import static com.example.backend.util.C.PET_STATUS_MIN;

@Data
public class PetStatusUpdateRequest {

    @NotNull
    private Long id;

    @NotNull
    private Long petId;

    @NotNull
    @Range(min = PET_STATUS_MIN, max = PET_STATUS_MAX, message = "错误状态")
    private Integer status;

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
