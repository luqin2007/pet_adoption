package com.example.backend.dto;

import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class FollowTaskQueryParams {

    private Long adopt;
    private Long volunteer;
    private Set<String> status;
    private Date time0;
    private Date time1;
}
