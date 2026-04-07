package com.example.backend.dto;

import com.example.backend.entity.Category;
import com.example.backend.entity.DonationItem;
import com.example.backend.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class DonationItemResponse implements IResponse {

    private Long id;
    private Long donationId;
    private BigDecimal count;
    private String description;
    private Date createTime;

    // item
    private Long itemId;
    private String itemName;
    private String itemUnit;

    // category
    private Long categoryId;
    private String categoryName;

    /**
     * Item: id, categoryId, name, unit<br>
     * Category: id, name
     */
    public static DonationItemResponse create(DonationItem donationItem, Item item, Category category) {
        return new DonationItemResponse(donationItem.getId(),
                donationItem.getDonationId(),
                donationItem.getCount(),
                donationItem.getDescription(),
                donationItem.getCreateTime(),
                item != null ? item.getId() : null,
                item != null ? item.getName() : donationItem.getItemName(),
                item != null ? item.getUnit() : donationItem.getUnit(),
                category.getId(),
                category.getName());
    }

    /**
     * Item: id, categoryId, name, unit<br>
     * Category: id, name<br>
     * <br>
     * items: DonationItem.itemId<br>
     * categories: Item.categoryId / DonationItem.categoryId
     */
    public static DonationItemResponse createBatch(DonationItem donationItem,
                                                   Map<Long, Item> items,
                                                   Map<Long, Category> categories) {
        Item item = items.get(donationItem.getItemId());
        Long categoryId = item != null ? item.getCategoryId() : donationItem.getCategoryId();
        return create(donationItem, item, categories.get(categoryId));
    }
}
