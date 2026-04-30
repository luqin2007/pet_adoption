package com.example.backend.dto;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DonationAddItemRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.item_donation.item.name")
    private String name;

    private Long itemId;

    // 当库存中不存在同种物品时，允许手动输入物品名
    private String itemName;

    // 当库存中不存在同种物品时，允许手动输入物品单位
    private String itemUnit;

    @NotNull(message = "request.item_donation.item.category")
    private Long categoryId;

    private String description;

    @NotBlank(message = "request.item_donation.count")
    private String count;

    @NotNull(message = "request.item_donation.expire_time")
    private Date expireTime;

    /**
     * Donation: id, createTime
     */
    public DonationItem create(Donation donation) {
        return new DonationItem(null,
                donation.getId(),
                itemId,
                itemName,
                categoryId,
                new BigDecimal(count),
                itemUnit,
                description,
                expireTime,
                donation.getCreateTime());
    }

    @Override
    public void validate(Errors errors) {
        if (itemId == null) { // 新物品
            if (itemName == null)
                errors.rejectValue("itemName", "request.item_donation.item.name");
            if (itemUnit == null)
                errors.rejectValue("itemUnit", "request.item_donation.unit");
        }

        if (expireTime.before(new Date())) // 已过期
            errors.rejectValue("expireTime", "request.item_donation.expired");
    }
}
