package com.example.backend.service;

import com.example.backend.util.IValidates;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;

public abstract class BaseServiceWithoutMapper implements IValidates {

    protected ObjectMapper objectMapper;

    @Autowired
    private void setObjects(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
