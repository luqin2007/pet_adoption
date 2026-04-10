package com.example.backend.dto;

import com.example.backend.entity.Breading;
import com.example.backend.entity.property.AdoptBreadingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class BreadingAddRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.pet.name")
    private String petName;

    @NotNull(message = "request.pet.age")
    @Min(value = 0, message = "request.pet.age")
    private Integer petAge;

    @NotBlank(message = "request.pet.type")
    private String petType;

    @NotBlank(message = "request.pet.breed")
    private String petBreed;

    private String petDescription;

    @NotBlank(message = "request.phone")
    private String applicantPhone;

    private String requirement;

    @NotNull(message = "request.start_time")
    private Date time0;

    @NotNull(message = "request.adopt_breading.breading.end_time")
    private Date time1;

    public Breading create(Long applicantId) {
        Date now = new Date();
        return new Breading(null,
                petName,
                petAge,
                petType,
                petBreed,
                petDescription,
                applicantId,
                applicantPhone,
                null,
                AdoptBreadingStatus.CREATE,
                requirement,
                null,
                null,
                time0,
                time1,
                now,
                now);
    }

    @Override
    public void validate(Errors errors) {
        validateTime(errors, BreadingAddRequest::getTime0, BreadingAddRequest::getTime1);
    }
}
