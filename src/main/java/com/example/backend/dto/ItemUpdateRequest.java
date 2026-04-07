package com.example.backend.dto;

import com.example.backend.entity.Item;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class ItemUpdateRequest implements IRequest {

    @NotBlank(message = "request.item_donation.item.name")
    private String name;

    @NotBlank(message = "request.item_donation.item.category")
    private Long categoryId;

    private String description;

    @NotBlank(message = "request.item_donation.unit")
    private String unit;

    public void applyTo(Item item) {
        item.setName(name);
        item.setCategoryId(categoryId);
        item.setDescription(description);
        item.setUnit(unit);
        item.setUpdateTime(new Date());
    }
}
