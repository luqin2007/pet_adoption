package com.example.backend.dto;

import com.example.backend.entity.AllergyHistory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class AllergyHistoryAddRequest implements IRequest {

    @NotBlank(message = "request.medical.allergy.source")
    private String source;

    @NotBlank(message = "request.medical.allergy.source")
    private String reaction;

    @NotNull(message = "request.medical.allergy.discovery")
    private Date discoveryTime;

    public AllergyHistory build(Long registrationId) {
        return new AllergyHistory(null,
                registrationId,
                source,
                reaction,
                discoveryTime,
                new Date());
    }
}
