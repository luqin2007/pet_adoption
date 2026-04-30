package com.example.backend.dto;

import com.example.backend.entity.*;
import com.example.backend.entity.property.StockAction;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

@Data
@AllArgsConstructor
public class StockRecordItemResponse implements IResponse {

    private Long id;
    private Long stockId;
    private StockAction action;
    private BigDecimal changeCount;
    private BigDecimal remainCount;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String purpose;
    private Date createTime;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * User: id, username, avatar
     */
    public static StockRecordItemResponse create(StockRecord record, User user) {
        return new StockRecordItemResponse(
                record.getId(),
                record.getStockId(),
                record.getAction(),
                record.getCount(),
                record.getRemain(),
                record.getPrice(),
                record.getTotalPrice(),
                record.getPurpose(),
                record.getCreateTime(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(USER, user.getId(), user.getAvatar()));
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: StockRecord.userId
     */
    public static StockRecordItemResponse createBatch(StockRecord record, Map<Long, User> users) {
        return create(record, users.get(record.getUserId()));
    }
}
