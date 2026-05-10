package com.example.backend.dto;

import com.example.backend.entity.property.FollowTaskStatus;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

@Data
public class FollowTaskQueryParams implements IParam, IValidatedRequest {

    private Long adopt;
    private Long volunteer;
    private Set<String> status;
    private Date time0;
    private Date time1;

    @Override
    public void validate(Errors errors) {
        validateEnums(errors, FollowTaskQueryParams::getStatus, FollowTaskStatus.class, "request.adopt_breading.follow_task.status");
        validateTime(errors, FollowTaskQueryParams::getTime0, FollowTaskQueryParams::getTime1);
    }
}
