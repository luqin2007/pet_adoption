package com.example.backend.dto;

import com.example.backend.entity.VaccineRecord;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class VaccineAddRequest {

    @NotNull(message = "未知疫苗")
    private Long vaccineId;

    @NotNull(message = "请输入第几次接种")
    @Min(value = 1, message = "请输入第几次接种")
    private Integer times;

    public VaccineRecord create(Long petId, Long userId) {
        return new VaccineRecord(null,
                petId,
                userId,
                vaccineId,
                times,
                new Date());
    }
}
