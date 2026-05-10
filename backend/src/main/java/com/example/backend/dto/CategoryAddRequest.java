package com.example.backend.dto;

import com.example.backend.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class CategoryAddRequest implements IRequest {

    @NotBlank(message = "request.item_donation.category.name")
    private String name;

    private String description;

    public Category create() {
        Date now = new Date();
        return new Category(null,
                name,
                description,
                false,
                now,
                now);
    }
}
