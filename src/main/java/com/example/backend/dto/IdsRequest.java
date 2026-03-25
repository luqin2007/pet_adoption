package com.example.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class IdsRequest {

    @NotEmpty
    private List<Long> ids = List.of();

    public Set<Long> idSet() {
        return ids.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
