package com.example.backend.dto;

import com.example.backend.entity.Subscribe;
import com.example.backend.entity.property.SubscribeAction;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

@Data
public class SubscribeQueryParams implements IParam<Subscribe>, IRequestValidate {

    private Set<Long> user;
    private Set<String> action;
    private Set<Long> element;
    private String min, max;
    private Date time0, time1;

    @Override
    public void validate(Errors errors) {
        validateEnums(errors, SubscribeQueryParams::getAction, SubscribeAction.class, "request.item_donation.subscribe.action");
        validateTime(errors, SubscribeQueryParams::getTime0, SubscribeQueryParams::getTime1);
        // element 非空时，action 需要 id 的有且只有一个
        if (!ObjectUtils.isEmpty(element)) {
            if (action == null || action.stream()
                    .map(SubscribeAction::get)
                    .filter(SubscribeAction::requireId).count() != 1)
                errors.rejectValue("action", "request.item_donation.subscribe.query");
        }
        // 限定 min/max 时，action 需要 count 的有且只有一个
        validateRange(errors, SubscribeQueryParams::getMin, SubscribeQueryParams::getMax, "request.item_donation.subscribe.query");
        if (min != null || max != null) {
            if (action == null || action.stream()
                    .map(SubscribeAction::get)
                    .filter(SubscribeAction::requireCount).count() != 1)
                errors.rejectValue("action", "request.item_donation.subscribe.query");
        }
    }
}
