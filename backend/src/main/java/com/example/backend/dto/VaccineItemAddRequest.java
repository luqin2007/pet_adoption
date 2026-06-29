package com.example.backend.dto;

import com.example.backend.entity.Vaccine;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class VaccineItemAddRequest implements IRequest {

    @NotNull(message = "request.item.id")
    private Long itemId;

    @NotBlank(message = "request.medical.vaccine.illness")
    private String illness;

    @Min(value = 0, message = "request.medical.vaccine.minAge")
    private Integer minAge;

    @Min(value = 1, message = "request.medical.vaccine.times")
    private Integer times;

    public Vaccine create() {
        Vaccine vaccine = new Vaccine();
        vaccine.setItemId(itemId);
        vaccine.setIllness(illness);
        vaccine.setMinAge(minAge != null ? minAge : 0);
        vaccine.setTimes(times != null ? times : 1);
        vaccine.setCreateTime(new Date());
        return vaccine;
    }
}
