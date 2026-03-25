package com.example.backend.dto;

import com.example.backend.entity.RescueTaskRecord;
import com.example.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescueTaskRecordsResponse {

    List<RescueTaskRecordResponse> records;

    public static RescueTaskRecordsResponse create(List<RescueTaskRecord> entities, Map<Long, User> users) {
        List<RescueTaskRecordResponse> result = entities.stream()
                .map(entity -> RescueTaskRecordResponse.createBatch(entity, users))
                .toList();
        return new RescueTaskRecordsResponse(result);
    }
}
