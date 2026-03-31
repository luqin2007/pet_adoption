package com.example.backend.dto;

import com.example.backend.entity.Adopt;
import com.example.backend.entity.property.AdoptBreadingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class AdoptAddRequest {

    @NotNull(message = "请选择宠物")
    private Long petId;

    @NotBlank(message = "请填写申请人手机号")
    private String applicantPhone;

    private String requirement;

    public Adopt create(Long applicantId) {
        Date now = new Date();
        return new Adopt(null,
                petId,
                applicantId,
                applicantPhone,
                null,
                AdoptBreadingStatus.CREATE,
                requirement,
                null,
                null,
                null,
                now,
                now);
    }
}
