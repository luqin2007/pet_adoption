package com.example.backend.dto;

import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class BreadingQueryParams {

    private Set<Long> applicant;
    private Set<Long> reviewer;
    private Set<String> status;
    private Set<String> petType;
    private Date time0;
    private Date time1;
}
