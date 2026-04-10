package com.example.backend.dto;

import com.example.backend.entity.ImmunityHistory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class ImmunityHistoryRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.medical.immunity.medicine")
    private String medicine;

    @NotBlank(message = "request.medical.immunity.illness")
    private String illness;

    @NotBlank(message = "request.medical.immunity.count")
    @Min(value = 1, message = "request.medical.immunity.count_total")
    private Integer count;

    @NotBlank(message = "request.medical.immunity.total")
    private Integer total;

    @NotBlank(message = "request.medical.immunity.time")
    private Date immunityTime;

    public ImmunityHistory createEntity(Long registrationId) {
        return new ImmunityHistory(null,
                registrationId,
                medicine,
                illness,
                count,
                total,
                immunityTime,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        if (total < count)
            errors.rejectValue("count", "request.medical.immunity.count_total");
    }
}
