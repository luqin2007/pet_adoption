package com.example.backend.dto;

import com.example.backend.entity.Order;
import com.example.backend.entity.property.OrderType;
import com.example.backend.entity.property.ParentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class OrderRequest {

    @NotNull(message = "请选择物品")
    private Long itemId;

    @NotBlank(message = "请选择物品类型")
    private String type;

    @NotNull(message = "请填写数量")
    @Min(value = 0, message = "数量错误")
    private Double count;

    @NotNull(message = "请填写单位")
    private String unit;

    @NotNull(message = "请填写价格")
    private Double price;

    public Order create(Long allowerId, Long parentId, ParentType parentType) {
        return new Order(null,
                allowerId,
                itemId,
                parentId,
                parentType,
                OrderType.get(type),
                count,
                unit,
                price,
                new Date());
    }
}
