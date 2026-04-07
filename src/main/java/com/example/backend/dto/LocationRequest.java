package com.example.backend.dto;

import com.example.backend.entity.Location;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Date;

@Data
public abstract class LocationRequest implements IRequest {

    // 地址信息
    @NotBlank(message = "request.location")
    private String province;

    @NotBlank(message = "request.location")
    private String city;

    @NotBlank(message = "request.location")
    private String county;

    @NotBlank(message = "request.location")
    private String detailAddress;

    public Location createLocation(Long parentId, Long userId) {
        return new Location(null,
                parentId,
                userId,
                province,
                city,
                county,
                detailAddress,
                new Date(System.currentTimeMillis()));
    }

    public void applyTo(Location location) {
        location.setProvince(getProvince());
        location.setCity(getCity());
        location.setCounty(getCounty());
        location.setDetailAddress(getDetailAddress());
        location.setCreateTime(new Date(System.currentTimeMillis()));
    }
}
