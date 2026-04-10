package com.example.backend.dto;

import com.example.backend.entity.AllergyHistory;
import com.example.backend.entity.FirstRegistration;
import com.example.backend.entity.ImmunityHistory;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.List;

@Data
public class FirstRegistrationAddRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.pet.id")
    private Long petId;

    @NotBlank(message = "request.pet.name")
    private String name;

    @NotBlank(message = "request.pet.age")
    private Integer age;

    @NotBlank(message = "request.medical.weight")
    private Double weight;

    @NotBlank(message = "request.medical.temperature")
    private Double temperature;

    // 免疫史
    private List<ImmunityHistoryRequest> immunities;

    // 过敏史
    private List<AllergyHistoryAddRequest> allergies;

    private String description;

    public FirstRegistration createEntity(Long userId) {
        return new FirstRegistration(null,
                userId,
                petId,
                name,
                age,
                weight,
                temperature,
                new Date());
    }

    public List<ImmunityHistory> createImmunityHistories(Long registrationId) {
        return immunities.stream().map(request -> request.createEntity(registrationId)).toList();
    }

    public List<AllergyHistory> createAllergyHistories(Long registrationId) {
        return allergies.stream().map(request -> request.build(registrationId)).toList();
    }

    @Override
    public void validate(Errors errors) {
        for (ImmunityHistoryRequest req : immunities) {
            req.validate(errors);
        }
    }
}
