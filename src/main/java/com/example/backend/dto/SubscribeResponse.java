package com.example.backend.dto;

import com.example.backend.entity.*;
import com.example.backend.entity.property.SubscribeAction;
import com.example.backend.util.FileUtils;
import com.example.backend.util.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

@Data
@AllArgsConstructor
public class SubscribeResponse implements IResponse {

    private Long id;
    private Date createTime;
    private SubscribeAction action;
    private String count;

    // item
    private Long itemId;
    private String itemName;

    // category
    private Long categoryId;
    private String categoryName;

    // stock
    private Long stockId;
    private Date inTime;
    private Long userId;
    private String username;
    private String avatar;

    /**
     * 订阅物品变化<br>
     * Item: id, categoryId, name<br>
     * Category: id, name
     */
    public static SubscribeResponse createItem(Subscribe subscribe, Item item, Category category) {
        return new SubscribeResponse(
                subscribe.getId(),
                subscribe.getCreateTime(),
                subscribe.getAction(),
                String.valueOf(subscribe.getCount()),
                item.getId(),
                item.getName(),
                category.getId(),
                category.getName(),
                null, null, null, null, null);
    }

    /**
     * 订阅分类变化<br>
     * Category: id, name
     */
    public static SubscribeResponse createCategory(Subscribe subscribe, Category category) {
        return new SubscribeResponse(
                subscribe.getId(),
                subscribe.getCreateTime(),
                subscribe.getAction(),
                String.valueOf(subscribe.getCount()),
                null, null,
                category.getId(),
                category.getName(),
                null, null, null, null, null);
    }

    /**
     * 库存变动变化<br>
     * Stock: id, userId, itemId, createTime<br>
     * Item: id, categoryId, name<br>
     * Category: id, name<br>
     * User: id, username, avatar
     */
    public static SubscribeResponse createStock(Subscribe subscribe, Stock stock, Item item, Category category, User user) {
        return new SubscribeResponse(
                subscribe.getId(),
                subscribe.getCreateTime(),
                subscribe.getAction(),
                String.valueOf(subscribe.getCount()),
                item.getId(),
                item.getName(),
                category.getId(),
                category.getName(),
                stock.getId(),
                stock.getCreateTime(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(USER, user.getId(), user.getAvatar()));
    }

    /**
     * 订阅用户捐赠<br>
     * User: id, username, avatar
     */
    public static SubscribeResponse createUser(Subscribe subscribe, User user) {
        return new SubscribeResponse(
                subscribe.getId(),
                subscribe.getCreateTime(),
                subscribe.getAction(),
                null, null, null,
                null, null,
                null, null,
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(USER, user.getId(), user.getAvatar()));
    }

    /**
     * Stock: id, userId, itemId, createTime<br>
     * Item: id, categoryId, name<br>
     * Category: id, name<br>
     * User: id, username, avatar<br>
     * <br>
     * stocks: Subscribe.elementId<br>
     * items: Subscribe.elementId, Stock.itemId<br>
     * categories: Subscribe.elementId, Item.categoryId<br>
     * users: Subscribe.elementId, Stock.userId
     */
    public static SubscribeResponse createBatch(Subscribe subscribe,
                                                Map<Long, Stock> stocks,
                                                Map<Long, Item> items,
                                                Map<Long, Category> categories,
                                                Map<Long, User> users) {
        SubscribeAction act = subscribe.getAction();
        if (act.bindItem()) { // ITEM_CHANGE, ITEM_COUNT
            Item item = items.get(subscribe.getElementId());
            Category category = categories.get(item.getCategoryId());
            return SubscribeResponse.createItem(subscribe, item, category);
        }
        if (act.bindCategory()) { // CATEGORY_COUNT
            Category category = categories.get(subscribe.getElementId());
            return SubscribeResponse.createCategory(subscribe, category);
        }
        if (act.bindStock()) { // IN_STOCK, OUT_STOCK
            Stock stock = stocks.get(subscribe.getElementId());
            Item item = items.get(stock.getItemId());
            Category category = categories.get(item.getCategoryId());
            User user = users.get(stock.getUserId());
            return SubscribeResponse.createStock(subscribe, stock, item, category, user);
        }
        if (act.bindUser()) { // DONATE
            User user = users.get(subscribe.getElementId());
            return SubscribeResponse.createUser(subscribe, user);
        }
        // Never here
        throw ServiceException.system("exception.system.unreachable");
    }
}
