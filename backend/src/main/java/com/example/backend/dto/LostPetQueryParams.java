package com.example.backend.dto;

import com.example.backend.entity.property.LostPetStatus;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 走失宠物报备查询参数
 */
@Data
public class LostPetQueryParams implements IParam, IValidatedRequest {

    private Set<Long> owner;

    private Set<String> status;

    private Set<String> type;

    private Set<String> bread;

    private String name;

    private Date time0;

    private Date time1;

    private String province;

    private String city;

    private String address;

    @Override
    public void validate(Errors errors) {
        validateTime(errors, LostPetQueryParams::getTime0, LostPetQueryParams::getTime1);
        validateEnums(errors, LostPetQueryParams::getStatus, LostPetStatus.class, "request.pet.status");
    }

    public boolean noLocation() {
        return province == null && city == null && address == null;
    }

    public boolean noPet() {
        return owner == null
                && status == null
                && type == null
                && bread == null
                && name == null
                && time0 == null
                && time1 == null;
    }
}
