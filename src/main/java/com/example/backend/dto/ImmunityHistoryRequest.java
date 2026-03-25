package com.example.backend.dto;

import com.example.backend.entity.ImmunityHistory;
import lombok.Data;

import java.util.Date;

@Data
public class ImmunityHistoryRequest {

    private String medicine;

    private Integer count;

    private Integer total;

    private Date immunityTime;

    public ImmunityHistory createEntity(Long registrationId) {
        return new ImmunityHistory(null,
                registrationId,
                medicine,
                count,
                total,
                immunityTime,
                new Date());
    }
}
