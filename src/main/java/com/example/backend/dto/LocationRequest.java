package com.example.backend.dto;

import com.example.backend.entity.Location;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Date;

@Data
public abstract class LocationRequest {

    // 地址信息
    @NotBlank(message = "请输入发现位置")
    private String province;
    @NotBlank(message = "请输入发现位置")
    private String city;
    @NotBlank(message = "请输入发现位置")
    private String county;
    @NotBlank(message = "请输入发现位置")
    private String detailAddress;

    public Location createLocation(Long parentId) {
        Location location = new Location();
        location.setParentId(parentId);
        location.setProvince(getProvince());
        location.setCity(getCity());
        location.setCounty(getCounty());
        location.setDetailAddress(getDetailAddress());
        location.setCreateTime(new Date(System.currentTimeMillis()));
        return location;
    }

    public void applyTo(Location location) {
        location.setProvince(getProvince());
        location.setCity(getCity());
        location.setCounty(getCounty());
        location.setDetailAddress(getDetailAddress());
        location.setCreateTime(new Date(System.currentTimeMillis()));
    }
}
