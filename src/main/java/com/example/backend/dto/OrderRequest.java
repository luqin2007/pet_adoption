package com.example.backend.dto;

import com.example.backend.entity.Order;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.Date;

import static com.example.backend.util.C.ORDER_TYPE_MAX;
import static com.example.backend.util.C.ORDER_TYPE_MIN;

@Data
public class OrderRequest {

    @NotNull(message = "请选择物品")
    private Long itemId;

    @NotNull(message = "请选择物品类型")
    @Range(min = ORDER_TYPE_MIN, max = ORDER_TYPE_MAX, message = "物品类型错误")
    private Integer type;

    @NotNull(message = "请填写数量")
    @Min(value = 0, message = "数量错误")
    private Double count;

    @NotNull(message = "请填写单位")
    private String unit;

    @NotNull(message = "请填写价格")
    private Double price;

    public Order create(Long allowerId, Long parentId, String parentType) {
        return new Order(null,
                allowerId,
                itemId,
                parentId,
                parentType,
                type,
                count,
                unit,
                price,
                new Date());
    }
}
