package com.example.backend.dto;

import com.example.backend.entity.Location;
import com.example.backend.entity.property.ParentType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class LocationRequest implements IRequest {

    // 地址信息
    @NotBlank(message = "request.location")
    protected String province;

    @NotBlank(message = "request.location")
    protected String city;

    @NotBlank(message = "request.location")
    protected String district;

    @NotBlank(message = "request.location")
    protected String detailAddress;

    public Location createLocation(ParentType parentType, Long parentId, Long userId) {
        return new Location(null,
                parentId,
                parentType,
                userId,
                province,
                city,
                district,
                detailAddress,
                new Date());
    }

    public void applyTo(Location location) {
        location.setProvince(getProvince());
        location.setCity(getCity());
        location.setDistrict(getDistrict());
        location.setDetailAddress(getDetailAddress());
        location.setCreateTime(new Date());
    }
}
