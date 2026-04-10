package com.example.backend.dto;

import com.example.backend.entity.Location;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Date;

@Data
public class LocationRequest implements IRequest {

    // 地址信息
    @NotBlank(message = "request.location")
    protected String province;

    @NotBlank(message = "request.location")
    protected String city;

    @NotBlank(message = "request.location")
    protected String county;

    @NotBlank(message = "request.location")
    protected String detailAddress;

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
