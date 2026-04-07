package com.example.backend.dto;

import com.example.backend.entity.Subscribe;
import com.example.backend.entity.property.SubscribeAction;
import com.example.backend.util.ServiceException;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SubscribeAddRequest implements IRequest, IRequestValidate {

    private Long elementId;

    @NotNull(message = "request.item_donation.subscribe.action")
    private String action;

    private String count;

    @Override
    public void validate(Errors errors) {
        try {
            SubscribeAction action = SubscribeAction.get(this.action);
            if (action.requireId() && elementId == null)
                errors.rejectValue("elementId", "request.item_donation.subscribe.id");
            if (action.requireCount()) {
                if (count == null)
                    errors.rejectValue("count", "request.item_donation.subscribe.count");
                validateNumber(errors, SubscribeAddRequest::getCount, "request.item_donation.subscribe.count");
            }
        } catch (ServiceException e) {
            errors.rejectValue("action", e.getMessage());
        }
    }

    public Subscribe create(Long userId) {
        return new Subscribe(null,
                elementId,
                userId,
                SubscribeAction.get(action),
                new BigDecimal(count),
                new Date());
    }
}
