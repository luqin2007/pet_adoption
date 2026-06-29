package com.example.backend.controller;

import com.example.backend.dto.ConfigPropertyRequest;
import com.example.backend.dto.Result;
import com.example.backend.entity.UserSetting;
import com.example.backend.service.SystemConfigService;
import com.example.backend.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final SystemConfigService configService;
    private final UserSettingService userSettingService;

    @GetMapping
    public Result<Map<String, String>> getAllConfig() {
        Map<String, String> config = configService.getAllConfig();
        return Result.success(config);
    }

    @GetMapping("/sys/{key}")
    public Result<String> getConfig(@PathVariable("key") String key) {
        String value = configService.getConfig(key);
        return Result.success(value);
    }

    @PutMapping(value = "/sys")
    public Result<Void> setConfig(ConfigPropertyRequest request) {
        configService.setConfig(request);
        return Result.success();
    }

    @GetMapping("/user/{id}")
    public Result<UserSetting> getUserConfig(@PathVariable("id") Long userId) {
        UserSetting settings = userSettingService.getOrCreate(userId);
        return Result.success(settings);
    }

    @PutMapping(value = "/user/{id}")
    public Result<Void> setConfig(@PathVariable("id") Long userId, @RequestBody UserSetting request) {
        userSettingService.setConfig(userId, request);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> setConfigs(@RequestBody Map<String, String> configs) {
        configService.setConfigs(configs);
        return Result.success();
    }
}
