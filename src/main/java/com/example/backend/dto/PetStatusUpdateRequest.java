package com.example.backend.dto;

import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.entity.property.PetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.sql.Date;

@Data
public class PetStatusUpdateRequest implements IRequest, IRequestValidate {

    @NotNull
    private Long id;

    @NotNull
    private Long petId;

    @NotBlank(message = "request.pet.status")
    private String status;

    @NotNull
    private String reason;

    /**
     * Pet: id, status
     */
    public PetStatusRecord applyTo(Pet pet, Long userId) {
        PetStatus oldStatus = pet.getStatus();
        pet.setStatus(PetStatus.get(status));
        return new PetStatusRecord(null,
                petId,
                userId,
                oldStatus,
                pet.getStatus(),
                reason,
                new Date(System.currentTimeMillis()));
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, PetStatusUpdateRequest::getStatus, PetStatus.class, "request.pet.status");
    }
}
