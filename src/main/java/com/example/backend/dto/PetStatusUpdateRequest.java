package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.entity.property.PetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PetStatusUpdateRequest extends StatusUpdateRequest {

    @NotNull
    private Long petId;

    /**
     * Pet: id, status
     */
    public PetStatusRecord create(Pet pet, Long userId) {
        return new PetStatusRecord(null,
                petId,
                userId,
                pet.getStatus(),
                PetStatus.get(status),
                reason,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, PetStatusUpdateRequest::getStatus, PetStatus.class, "request.pet.status");
    }
}
