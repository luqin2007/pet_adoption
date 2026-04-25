package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 省市区信息
 */
@Data
@AllArgsConstructor
public class InfoCity {

    private String province;
    private String city;
    private String district;
}
