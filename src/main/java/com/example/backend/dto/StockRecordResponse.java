package com.example.backend.dto;

import com.example.backend.entity.*;
import com.example.backend.entity.property.SourceType;
import com.example.backend.entity.property.StockRecordAction;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

@Data
@AllArgsConstructor
public class StockRecordResponse implements IResponse {

    private Long id;
    private StockRecordAction action;
    private BigDecimal count;
    private BigDecimal remainCount;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String purpose;
    private Date createTime;

    // stock
    private Long stockId;
    private SourceType sourceType;
    private Date expireTime;
    private Date stockCreateTime;

    // item
    private Long itemId;
    private String itemName;

    // category
    private Long categoryId;
    private String categoryName;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * Stock: id, sourceType, expireTime, createTime<br>
     * Item: id, name<br>
     * Category: id, name<br>
     * User: id, username, avatar
     */
    public static StockRecordResponse create(StockRecord record, Stock stock, Item item, Category category, User user) {
        return new StockRecordResponse(
                record.getId(),
                record.getAction(),
                record.getCount(),
                record.getRemainCount(),
                record.getPrice(),
                record.getTotalPrice(),
                record.getPurpose(),
                record.getCreateTime(),
                stock.getId(),
                stock.getSourceType(),
                stock.getExpireTime(),
                stock.getCreateTime(),
                item.getId(),
                item.getName(),
                category.getId(),
                category.getName(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(USER, user.getId(), user.getAvatar()));
    }

    /**
     * Stock: id, itemId, sourceType, expireTime, createTime<br>
     * Item: id, categoryId, name<br>
     * Category: id, name<br>
     * User: id, username, avatar<br>
     * <br>
     * stock: StockRecord.stockId<br>
     * item: Stock.itemId<br>
     * category: Item.categoryId<br>
     * users: StockRecord.userId
     */
    public static StockRecordResponse createBatch(StockRecord record,
                                                  Map<Long, Stock> stocks,
                                                  Map<Long, Item> items,
                                                  Map<Long, Category> categories,
                                                  Map<Long, User> users) {
        Stock stock = stocks.get(record.getStockId());
        Item item = items.get(stock.getItemId());
        return create(record, stock, item, categories.get(item.getCategoryId()), users.get(record.getUserId()));
    }
}
