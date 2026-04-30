package com.example.backend.dto;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationItem;
import com.example.backend.entity.property.DeliveryType;
import com.example.backend.entity.property.DonationStatus;
import com.example.backend.util.ServiceException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.List;

@Data
public class DonationAddRequest implements IRequest, IValidatedRequest {

    @NotEmpty(message = "request.timeout")
    private String uuid;

    @NotEmpty(message = "request.item_donation.donation.items")
    private List<DonationAddItemRequest> items;

    @NotBlank(message = "request.item_donation.donation.delivery")
    private String delivery;

    private String description;

    // 交付位置或快递单号
    private String address;
    private String trackingNumber;

    public Donation create(Long userId) {
        Date now = new Date();
        return new Donation(null,
                userId,
                DeliveryType.get(delivery),
                address,
                trackingNumber,
                description,
                DonationStatus.CREATED,
                now,
                now);
    }

    /**
     * Donation: id, createTime
     */
    public List<DonationItem> createItems(Donation donation) {
        return items.stream()
                .map(item -> item.create(donation))
                .toList();
    }

    @Override
    public void validate(Errors errors) {
        // 校验捐赠物品
        for (DonationAddItemRequest item : items)
            item.validate(errors);
        // 校验交付方式
        try {
            DeliveryType type = DeliveryType.get(delivery);
            switch (type) {
                case EXPRESS -> { // 快递
                    if (ObjectUtils.isEmpty(trackingNumber))
                        errors.rejectValue("trackingNumber", "request.item_donation.donation.tracking_number");
                }
                case ADDRESS -> { // 指定地址交付
                    if (ObjectUtils.isEmpty(address))
                        errors.rejectValue("address", "request.item_donation.donation.position");
                }
            }
        } catch (ServiceException e) {
            errors.rejectValue("delivery", "request.item_donation.donation.delivery");
        }
    }
}
