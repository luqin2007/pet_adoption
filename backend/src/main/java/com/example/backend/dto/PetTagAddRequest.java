package com.example.backend.dto;

import com.example.backend.entity.PetTag;
import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class PetTagAddRequest implements IRequest {

    @NotEmpty(message = "request.pet.tags")
    private List<String> tags;

    public List<PetTag> create(Long petId, Long userId, Set<String> currentTags) {
        Date now = new Date();
        return tags.stream()
                // 非空
                .filter(StringUtils::hasText)
                // 去重
                .distinct()
                .filter(tag -> !currentTags.contains(tag))
                .map(tag -> new PetTag(null, petId, userId, tag, now))
                .toList();
    }
}
