package com.example.backend.dto;

import com.example.backend.entity.Item;
import com.example.backend.entity.Vaccine;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VaccineOptionResponse implements IResponse {

    private Long id;
    private Long itemId;
    private String name;
    private String illness;
    private Integer minAge;
    private Integer times;

    public static VaccineOptionResponse create(Vaccine vaccine, Item item) {
        return new VaccineOptionResponse(
                vaccine.getId(),
                vaccine.getItemId(),
                item.getName(),
                vaccine.getIllness(),
                vaccine.getMinAge(),
                vaccine.getTimes());
    }
}
