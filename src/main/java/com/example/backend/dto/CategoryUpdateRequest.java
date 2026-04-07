package com.example.backend.dto;

import com.example.backend.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class CategoryUpdateRequest implements IRequest {

    @NotBlank(message = "request.item_donation.category.name")
    private String name;

    private String description;

    public void applyTo(Category category) {
        category.setName(name);
        category.setDescription(description);
        category.setUpdateTime(new Date());
    }
}
