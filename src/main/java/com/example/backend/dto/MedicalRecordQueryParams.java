package com.example.backend.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MedicalRecordQueryParams {

    /**
     * 流浪宠物 id
     */
    private Long pet;

    /**
     * 接诊人 id
     */
    private Long doctor;

    /**
     * 记录状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date time0, time1;
}
