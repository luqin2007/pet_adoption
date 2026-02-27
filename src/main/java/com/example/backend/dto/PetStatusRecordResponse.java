package com.example.backend.dto;

import com.example.backend.entity.PetImage;
import com.example.backend.entity.PetInformation;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.entity.User;
import com.example.backend.util.FileUtils;
import lombok.Data;

import java.nio.file.Path;
import java.util.Date;

@Data
public class PetStatusRecordResponse {
    Long id;
    int from;
    int to;
    Date time;
    String description;

    Long petId;
    String pet;
    String cover;

    Long userId;
    String user;
    String email;
    Integer role;
    String avatar;

    public static PetStatusRecordResponse fromEntity(PetStatusRecord record, PetInformation petInformation,
                                                     PetImage cover, User user, FileUtils fileUtils) {
        PetStatusRecordResponse response = new PetStatusRecordResponse();
        response.setId(record.getId());
        response.setFrom(record.getFrom());
        response.setTo(record.getTo());
        response.setTime(record.getTime());
        response.setDescription(record.getDescription());

        response.setPetId(record.getPetId());
        response.setPet(petInformation.getName());
        if (cover != null) {
            Path imagePath = fileUtils.buildPetImagePath(cover.getPetId());
            response.setCover(cover.toAssetUrl(imagePath));
        }

        response.setUserId(record.getUserId());
        response.setUser(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());

        return response;
    }
}
