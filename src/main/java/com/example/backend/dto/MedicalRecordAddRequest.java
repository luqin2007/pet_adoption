package com.example.backend.dto;

import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.property.MedicalRecordStatus;
import com.example.backend.entity.property.MedicalRecordType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MedicalRecordAddRequest implements IRequest, IRequestValidate {

    @NotNull(message = "request.pet.age")
    @Min(value = 0, message = "request.pet.age")
    private Integer petAge;

    private Long ownerId;

    private String ownerPhone;

    private String price;

    private String cost;

    @NotBlank(message = "request.medical.record.type")
    private String type;

    public MedicalRecord createEntity(Long petId, Long doctorId) {
        Date now = new Date();
        return new MedicalRecord(null,
                petId,
                petAge,
                doctorId,
                ownerId,
                ownerPhone,
                MedicalRecordStatus.WAITING,
                MedicalRecordType.get(type),
                null,
                null,
                price == null ? BigDecimal.ZERO : new BigDecimal(price),
                cost == null ? BigDecimal.ZERO : new BigDecimal(cost),
                now,
                now);
    }

    @Override
    public void validate(Errors errors) {
        validateNumber(errors, MedicalRecordAddRequest::getPrice, "request.price");
        validateNumber(errors, MedicalRecordAddRequest::getCost, "request.price");
        validateEnum(errors, MedicalRecordAddRequest::getType, MedicalRecordType.class, "request.medical.record.type");
    }
}
