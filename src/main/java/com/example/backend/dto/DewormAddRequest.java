package com.example.backend.dto;

import com.example.backend.entity.DewormRecord;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class DewormAddRequest implements IRequest {

    @NotNull(message = "request.medical.deworm.dewormer")
    private Long dewormerId;

    @NotNull(message = "request.medical.deworm.times")
    @Min(value = 1, message = "request.medical.deworm.times")
    private Integer times;

    public DewormRecord create(Long petId, Long userId) {
        return new DewormRecord(null,
                petId,
                userId,
                dewormerId,
                times,
                new Date());
    }
}
