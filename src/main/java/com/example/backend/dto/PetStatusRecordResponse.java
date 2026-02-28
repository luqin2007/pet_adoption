package com.example.backend.dto;

import com.example.backend.entity.PetInformation;
import com.example.backend.entity.PetStatusRecord;
import lombok.Data;

import java.util.Date;

@Data
public class PetStatusRecordResponse {

    // 状态记录
    Long id;
    int from;
    int to;
    Date time;
    String description;

    // 对应宠物信息
    Long petId;
    String petName;
    String cover;

    // 发起用户信息
    UserResponse user;

    public static PetStatusRecordResponse fromEntity(PetStatusRecord record, PetInformation petInformation, String cover, UserResponse user) {
        PetStatusRecordResponse response = new PetStatusRecordResponse();
        response.setId(record.getId());
        response.setFrom(record.getFrom());
        response.setTo(record.getTo());
        response.setTime(record.getTime());
        response.setDescription(record.getDescription());
        response.setPetId(record.getPetId());
        response.setPetName(petInformation.getName());
        response.setCover(cover);
        response.setUser(user);
        return response;
    }
}
