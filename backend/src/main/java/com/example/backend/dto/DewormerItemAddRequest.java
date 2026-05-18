package com.example.backend.dto;

import com.example.backend.entity.Dewormer;
import com.example.backend.entity.property.DewormerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class DewormerItemAddRequest implements IRequest {

    @NotNull(message = "request.item.id")
    private Long itemId;

    @NotNull(message = "request.medical.dewormer.type")
    private DewormerType type;

    @Min(value = 0, message = "request.medical.dewormer.minAge")
    private Integer minAge;

    @Min(value = 1, message = "request.medical.dewormer.times")
    private Integer times;

    public Dewormer create() {
        Dewormer dewormer = new Dewormer();
        dewormer.setItemId(itemId);
        dewormer.setType(type);
        dewormer.setMinAge(minAge != null ? minAge : 0);
        dewormer.setTimes(times != null ? times : 1);
        dewormer.setCreateTime(new Date());
        return dewormer;
    }
}
