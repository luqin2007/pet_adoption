package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
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
        PetStatusRecord record = new PetStatusRecord();
        record.setUserId(userId);
        record.setPetId(info.getId());
        record.setFrom(info.getStatus());
        record.setTo(status);
        record.setCreateTime(new Date(System.currentTimeMillis()));
        record.setDescription(reason);
        return record;
    }
}
