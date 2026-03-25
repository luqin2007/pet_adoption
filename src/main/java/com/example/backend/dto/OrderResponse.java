package com.example.backend.dto;

import com.example.backend.entity.Item;
import com.example.backend.entity.Order;
import com.example.backend.entity.User;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.util.C.PARENT_USER;

@Data
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Integer type;
    private Double count;
    private String unit;
    private Double price;
    private Date createTime;
    private Long parentId;
    private String parentType;

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
                order.getCount(),
                order.getUnit(),
                order.getPrice(),
                order.getCreateTime(),
                order.getParentId(),
                order.getParentType(),
                allower.getId(),
                allower.getUsername(),
                FileUtils.generateAssetUrl(PARENT_USER, allower.getId(), allower.getAvatar()),
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
