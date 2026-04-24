package com.example.backend.dto;

import com.example.backend.entity.Stock;
import com.example.backend.entity.StockRecord;
import com.example.backend.entity.property.StockAction;
import com.example.backend.entity.property.SourceType;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StockRequest implements IRequest, IValidatedRequest {

    /**
     * 使用已有库存
     */
    private Long id;

    @NotNull(message = "request.item_donation.item")
    private Long itemId;

    @NotNull(message = "request.item_donation.count")
    @Min(value = 0, message = "request.item_donation.count.min")
    private String count;

    @NotBlank(message = "request.item_donation.stock.action")
    private String action;

    private String sourceType;

    @NotBlank(message = "request.item_donation.stock.purpose")
    private String purpose;

    private Date expireTime;

    private String price;

    private String totalPrice;

    public Stock createEmptyStock(Long userId) {
        Date now = new Date();
        return new Stock(null,
                itemId,
                userId,
                BigDecimal.ZERO,
                SourceType.get(sourceType),
                expireTime,
                now,
                now);
    }

    public StockRecord createRecord(Stock stock, Long operatorId) {
        return new StockRecord(null,
                stock.getId(),
                operatorId,
                StockAction.get(action),
                SourceType.get(sourceType),
                new BigDecimal(count),
                stock.getCount(),
                StringUtils.hasText(price) ? new BigDecimal(price) : BigDecimal.ZERO,
                StringUtils.hasText(totalPrice) ? new BigDecimal(totalPrice) : BigDecimal.ZERO,
                purpose,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        if (expireTime != null && expireTime.before(new Date()))
            errors.rejectValue("expireTime", "request.item_donation.expired");
        validateNumber(errors, StockRequest::getPrice, "request.price");
        validateNumber(errors, StockRequest::getTotalPrice, "request.price");

        try { // 入库、出库、销毁操作校验
            StockAction action = StockAction.get(this.action);
            if (action == StockAction.IN) {
                // 物资来源
                if (sourceType == null) {
                    errors.rejectValue("sourceType", "request.item_donation.stock.source_type");
                } else {
                    SourceType type = SourceType.get(sourceType);
                    if (type == SourceType.PURCHASE) { // 采购
                        if (price == null) // 单价
                            errors.rejectValue("price", "request.price");
                        else
                            validateNumber(errors, StockRequest::getPrice, "request.price");
                        if (totalPrice == null) // 总价
                            errors.rejectValue("totalPrice", "request.item_donation.stock.total_price");
                        else
                            validateNumber(errors, StockRequest::getTotalPrice, "request.item_donation.stock.total_price");
                    }
                }
                // 保质期
                if (expireTime == null)
                    errors.rejectValue("expireTime", "request.item_donation.expire_time");
                else if (expireTime.before(new Date()))
                    errors.rejectValue("expireTime", "request.item_donation.expired");
            } else { // 出库、销毁：要求必须有 id
                if (id == null)
                    errors.rejectValue("id", "request.item_donation.stock.id");
            }
        } catch (ServiceException e) {
            errors.rejectValue("action", e.getMessage());
        }
    }
}
