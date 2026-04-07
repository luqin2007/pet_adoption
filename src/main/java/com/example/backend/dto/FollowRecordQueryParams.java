package com.example.backend.dto;

import com.example.backend.entity.FollowRecord;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class FollowRecordQueryParams implements IParam<FollowRecord>, IRequestValidate {

    private Long task;
    private Long volunteer;
    private Date time0;
    private Date time1;

    @Override
    public void validate(Errors errors) {
        validateTime(errors, FollowRecordQueryParams::getTime0, FollowRecordQueryParams::getTime1);
    }
}
