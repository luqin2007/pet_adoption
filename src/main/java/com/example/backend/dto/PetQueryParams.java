package com.example.backend.dto;

import com.example.backend.entity.property.PetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetQueryParams implements IParam, IValidatedRequest {

    // Pet
    private Set<Long> user;
    private Integer age0, age1; // age0 <= age <= age1
    private String sex;
    private Set<String> type;
    private Set<String> breed;
    private Set<String> status;
    private String name; // like name, description

    // Location
    private String province;
    private String city;
    private String county;
    private String address; // like detailAddress
    private Date time; // ge createTime

    @Override
    public void validate(Errors errors) {
        validateEnums(errors, PetQueryParams::getStatus, PetStatus.class, "request.pet.status");
        validateIntRange(errors, PetQueryParams::getAge0, PetQueryParams::getAge1, "request.pet.age");
    }
}
