package com.example.backend.dto;

import com.example.backend.entity.Item;
import com.example.backend.entity.Order;
import com.example.backend.entity.User;
import com.example.backend.entity.property.OrderType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class OrderResponse implements IResponse {

    private Long id;
    private OrderType type;
    private String count;
    private String unit;
    private String price;
    private Date createTime;
    private Long parentId;
    private ParentType parentType;

    // user
    private Long allowerId;
    private String allerName;
    private String allerAvatar;

    // item
    private Long itemId;
    private String itemName;

    /**
     * User: id, name, avatar<br>
     * Item: id, name
     */
    public static OrderResponse create(Order order, User allower, Item item) {
        return new OrderResponse(order.getId(),
                order.getType(),
                String.valueOf(order.getCount()),
                order.getUnit(),
                String.valueOf(order.getPrice()),
                order.getCreateTime(),
                order.getParentId(),
                order.getParentType(),
                allower.getId(),
                allower.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, allower.getId(), allower.getAvatar()),
                item.getId(),
                item.getName());
    }

    /**
     * User: id, name, avatar<br>
     * Item: id, name<br>
     * <br>
     * users: Order.allowerId<br>
     * items: Order.itemId
     */
    public static OrderResponse createBatch(Order order, Map<Long, User> users, Map<Long, Item> items) {
        return create(order, users.get(order.getAllowerId()), items.get(order.getItemId()));
    }
}
