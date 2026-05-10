package com.example.backend.dto;

import com.example.backend.entity.Category;
import com.example.backend.entity.Item;
import com.example.backend.entity.Stock;
import com.example.backend.entity.User;
import com.example.backend.entity.property.SourceType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

/**
 * 库存批次响应体
 */
@Data
@AllArgsConstructor
public class StockResponse implements IResponse {

    private Long id;
    private String count;
    private SourceType sourceType;
    private Date expireTime;
    private Date createTime;
    private Date updateTime;
    private List<StockRecordItemResponse> records;

    // item
    private Long itemId;
    private String itemName;
    private String unit;

    // category
    private Long categoryId;
    private String categoryName;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * Item: id, categoryId, name, unit, categoryId<br>
     * Category: id, name<br>
     * User: id, username, avatar
     */
    public static StockResponse create(Stock stock, Item item, Category category, User user, List<StockRecordItemResponse> records) {
        return new StockResponse(
                stock.getId(),
                String.valueOf(stock.getCount()),
                stock.getSourceType(),
                stock.getExpireTime(),
                stock.getCreateTime(),
                stock.getUpdateTime(),
                records,
                item.getId(),
                item.getName(),
                item.getUnit(),
                category.getId(),
                category.getName(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(USER, user.getId(), user.getAvatar()));
    }

    /**
     * Item: id, name, unit, categoryId<br>
     * Category: id, name<br>
     * User: id, username, avatar<br>
     * <br>
     * items: Stock.itemId<br>
     * categories: Item.categoryId<br>
     * users: Stock.userId<br>
     * records: stock.id
     */
    public static StockResponse createBatch(Stock stock,
                                            Map<Long, Item> items, Map<Long, Category> categories,
                                            Map<Long, User> users,
                                            Map<Long, List<StockRecordItemResponse>> records) {
        Item item = items.get(stock.getItemId());
        return create(stock,
                item, categories.get(item.getCategoryId()),
                users.get(stock.getUserId()),
                records.get(stock.getId()));
    }
}
