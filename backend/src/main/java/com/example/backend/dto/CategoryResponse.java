package com.example.backend.dto;

import com.example.backend.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class CategoryResponse implements IResponse {

    private Long id;
    private String name;
    private String description;
    private Date createTime;
    private Date updateTime;

    public static CategoryResponse create(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreateTime(),
                category.getUpdateTime());
    }
}
