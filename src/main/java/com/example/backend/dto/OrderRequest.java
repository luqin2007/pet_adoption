package com.example.backend.dto;

import com.example.backend.entity.Order;
import com.example.backend.entity.property.OrderType;
import com.example.backend.entity.property.ParentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderRequest implements IRequest, IRequestValidate {

    @NotNull(message = "request.item_donation.item")
    private Long itemId;

    @NotBlank(message = "request.item_donation.order.type")
    private String type;

    @NotNull(message = "request.item_donation.count")
    private String count;

    @NotNull(message = "request.item_donation.unit")
    private String unit;

    @NotNull(message = "request.item_donation.order.price")
    private String price;

    public Order create(Long allowerId, Long parentId, ParentType parentType) {
        return new Order(null,
                allowerId,
                itemId,
                parentId,
                parentType,
                OrderType.get(type),
                new BigDecimal(count),
                unit,
                new BigDecimal(price),
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateNumber(errors, OrderRequest::getCount, "request.item_donation.count");
        validateNumber(errors, OrderRequest::getPrice, "request.item_donation.order.price");
    }
}
