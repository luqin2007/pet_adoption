package com.example.backend.dto;

import com.example.backend.entity.DewormRecord;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class DewormAddRequest {

    @NotNull(message = "未知驱虫药")
    private Long dewormerId;

    @NotNull(message = "请输入第几次驱虫")
    @Min(value = 1, message = "请输入第几次驱虫")
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
