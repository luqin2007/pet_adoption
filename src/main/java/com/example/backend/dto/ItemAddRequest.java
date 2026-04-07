package com.example.backend.dto;

import com.example.backend.entity.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class ItemAddRequest implements IRequest {

    @NotBlank(message = "request.item_donation.item.name")
    private String name;

    @NotNull(message = "request.item_donation.item.category")
    private Long categoryId;

    private String description;

    @NotBlank(message = "request.item_donation.unit")
    private String unit;

    public Item create() {
        Date now = new Date();
        return new Item(null,
                categoryId,
                name,
                description,
                unit,
                false,
                now,
                now);
    }
}
