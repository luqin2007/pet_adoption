package com.example.backend.dto;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationItem;
import com.example.backend.entity.property.DeliveryType;
import com.example.backend.util.ServiceException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Data
public class DonationUpdateRequest implements IRequest, IRequestValidate {

    @NotEmpty(message = "request.item_donation.donation.items")
    private List<DonationAddItemRequest> items;

    @NotBlank(message = "request.item_donation.donation.delivery")
    private String delivery;

    private String description;

    // 交付位置或快递单号
    private String address;
    private String trackingNumber;

    public void apply(Donation donation) {
        donation.setDelivery(DeliveryType.get(delivery));
        donation.setDescription(description);
        donation.setAddress(address);
        donation.setTrackingNumber(trackingNumber);
        donation.setUpdateTime(new Date());
    }

    /**
     * Donation: id, createTime
     */
    public List<DonationItem> createItems(Donation donation) {
        return Stream.ofNullable(items)
                .flatMap(List::stream)
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
