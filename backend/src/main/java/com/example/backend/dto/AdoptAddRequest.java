package com.example.backend.dto;

import com.example.backend.entity.Adopt;
import com.example.backend.entity.property.AdoptBreadingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdoptAddRequest extends LocationRequest implements IRequest {

    @NotNull(message = "request.pet.id")
    private Long petId;

    @NotBlank(message = "request.phone")
    private String applicantPhone;

    public Adopt create(Long applicantId) {
        Date now = new Date();
        return new Adopt(null,
                petId,
                applicantId,
                applicantPhone,
                null,
                AdoptBreadingStatus.CREATE,
                null,
                null,
                null,
                now,
                now);
    }
}
