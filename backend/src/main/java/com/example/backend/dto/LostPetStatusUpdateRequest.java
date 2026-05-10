package com.example.backend.dto;

import com.example.backend.entity.property.LostPetStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

@Data
@EqualsAndHashCode(callSuper = true)
public class LostPetStatusUpdateRequest extends StatusUpdateRequest {

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, LostPetStatusUpdateRequest::getStatus, LostPetStatus.class, "request.pet.status");
    }
}
