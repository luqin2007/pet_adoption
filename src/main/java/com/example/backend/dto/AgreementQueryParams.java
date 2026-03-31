package com.example.backend.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AgreementQueryParams {

    private Long parentId;
    private String parentType;
    private Boolean signed;
    private Date time0;
    private Date time1;
}
