package com.example.backend.dto;

import com.example.backend.entity.PetTag;
import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.sql.Date;
import java.util.List;
import java.util.Set;

@Data
public class PetTagNamesRequest {

    @NotEmpty(message = "请输入宠物特征")
    private List<String> tags;

    public List<PetTag> createTags(Long petId, Long userId, Set<String> currentTags) {
        Date now = new Date(System.currentTimeMillis());
        return tags.stream()
                // 非空
                .filter(StringUtils::hasText)
                // 去重
                .distinct()
                .filter(tag -> !currentTags.contains(tag))
                .map(tag -> {
                    PetTag petTag = new PetTag();
                    petTag.setPetId(petId);
                    petTag.setUserId(userId);
                    petTag.setTag(tag);
                    petTag.setCreateTime(now);
                    return petTag;
                })
                .toList();
    }
}
