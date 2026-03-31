package com.example.backend.dto;

import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class AdoptQueryParams {

    private Set<Long> pet;
    private Set<Long> applicant;
    private Set<String> status;
    private Date time0;
    private Date time1;
}
