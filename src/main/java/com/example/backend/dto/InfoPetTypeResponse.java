package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InfoPetTypeResponse implements IResponse {

    private String type;
    private List<String> breeds;
}
