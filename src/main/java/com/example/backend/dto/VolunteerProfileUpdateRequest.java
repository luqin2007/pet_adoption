package com.example.backend.dto;

import com.example.backend.entity.VolunteerProfile;
import lombok.Data;

import java.util.Date;

/**
 * 志愿者档案更新请求
 */
@Data
public class VolunteerProfileUpdateRequest implements IRequest {

    private String realName;
    private String sex;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String address;
    private String skills;
    private String serviceIntention;
    private String availableTimeDesc;
    private String remark;

    /**
     * 将请求内容应用到志愿者档案
     */
    public void applyTo(VolunteerProfile profile) {
        if (realName != null) profile.setRealName(realName);
        if (sex != null) profile.setSex(sex);
        if (phone != null) profile.setPhone(phone);
        if (province != null) profile.setProvince(province);
        if (city != null) profile.setCity(city);
        if (district != null) profile.setDistrict(district);
        if (address != null) profile.setAddress(address);
        if (skills != null) profile.setSkills(skills);
        if (serviceIntention != null) profile.setServiceIntention(serviceIntention);
        if (availableTimeDesc != null) profile.setAvailableTimeDesc(availableTimeDesc);
        if (remark != null) profile.setRemark(remark);
        profile.setUpdateTime(new Date());
    }
}
