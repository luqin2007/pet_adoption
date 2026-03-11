package com.example.backend.dto;

import com.example.backend.entity.RescueTaskRecord;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RescueTaskRecordsResponse {

    List<RescueTaskRecordResponse> records;

    public static RescueTaskRecordsResponse fromEntity(List<RescueTaskRecord> entities, Map<Long, UsernameAndAvatarResponse> users) {
        RescueTaskRecordsResponse response = new RescueTaskRecordsResponse();
        List<RescueTaskRecordResponse> result = entities.stream()
                .map(entity -> RescueTaskRecordResponse.fromEntity(entity, users))
                .toList();
        response.setRecords(result);
        return null;
    }
}
