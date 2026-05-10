package com.example.backend.dto;

import com.example.backend.entity.Dewormer;
import com.example.backend.entity.Item;
import com.example.backend.entity.property.DewormerType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DewormerOptionResponse implements IResponse {

    private Long id;
    private Long itemId;
    private String name;
    private DewormerType type;
    private Integer minAge;
    private Integer times;

    public static DewormerOptionResponse create(Dewormer dewormer, Item item) {
        return new DewormerOptionResponse(
                dewormer.getId(),
                dewormer.getItemId(),
                item.getName(),
                dewormer.getType(),
                dewormer.getMinAge(),
                dewormer.getTimes());
    }
}
