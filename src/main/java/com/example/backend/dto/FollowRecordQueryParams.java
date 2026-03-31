package com.example.backend.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FollowRecordQueryParams {

    private Long task;
    private Long volunteer;
    private Date time0;
    private Date time1;
}
