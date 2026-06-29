package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig implements IId {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private Date updatedAt;
}
