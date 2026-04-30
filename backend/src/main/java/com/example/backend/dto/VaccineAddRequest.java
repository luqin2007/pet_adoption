package com.example.backend.dto;

import com.example.backend.entity.VaccineRecord;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class VaccineAddRequest implements IRequest {

    @NotNull(message = "request.medical.vaccine.id")
    private Long vaccineId;

    @NotNull(message = "request.pet.age")
    @Min(value = 0, message = "request.pet.age")
    private Integer petAge;

    @NotNull(message = "request.medical.vaccine.times")
    @Min(value = 1, message = "request.medical.vaccine.times")
    private Integer times;

    public VaccineRecord create(Long petId, Long userId) {
        return new VaccineRecord(null,
                petId,
                petAge,
                userId,
                vaccineId,
                times,
                new Date());
    }
}
