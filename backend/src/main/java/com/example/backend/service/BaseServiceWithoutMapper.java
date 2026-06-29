package com.example.backend.service;

import com.example.backend.util.IValidates;
import com.example.backend.util.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;

public abstract class BaseServiceWithoutMapper implements IValidates {

    protected ObjectMapper objectMapper;
    protected RedisHelper redisHelper;

    @Autowired
    private void setObjects(ObjectMapper objectMapper, RedisHelper redisHelper) {
        this.objectMapper = objectMapper;
        this.redisHelper = redisHelper;
    }
}
