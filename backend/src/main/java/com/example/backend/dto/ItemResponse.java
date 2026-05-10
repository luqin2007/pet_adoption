package com.example.backend.dto;

import com.example.backend.entity.Category;
import com.example.backend.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 物资响应体
 */
@Data
@AllArgsConstructor
public class ItemResponse implements IResponse {

    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String unit;
    private Boolean discard;
    private Date createTime;
    private Date updateTime;

    /**
     * Category: id, name
     */
    public static ItemResponse create(Item item, Category category) {
        return new ItemResponse(item.getId(),
                item.getName(),
                item.getCategoryId(),
                category.getName(),
                item.getDescription(),
                item.getUnit(),
                item.getIsDiscard(),
                item.getCreateTime(),
                item.getUpdateTime());
    }

    /**
     * Category: id, name<br>
     * <br>
     * categories: Item.categoryId
     */
    public static ItemResponse createBatch(Item item, Map<Long, Category> categories) {
        return create(item, categories.get(item.getCategoryId()));
    }
}
