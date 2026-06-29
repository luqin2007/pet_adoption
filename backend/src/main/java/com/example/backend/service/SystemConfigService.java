package com.example.backend.service;

import com.example.backend.dto.ConfigPropertyRequest;
import com.example.backend.entity.SystemConfig;
import com.example.backend.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigService extends BaseService<SystemConfigMapper, SystemConfig> {

    public Map<String, String> getAllConfig() {
        List<SystemConfig> list = list();
        Map<String, String> map = new HashMap<>(list.size());
        for (SystemConfig c : list) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return map;
    }

    public String getConfig(String key) {
        SystemConfig c = baseMapper.lambdaQuery()
                .eq(SystemConfig::getConfigKey, key).one();
        return c == null ? null : c.getConfigValue();
    }

    @Transactional
    public void setConfig(ConfigPropertyRequest request) {
        requirePermission(requireLoginUser().isWorker());
        SystemConfig c = baseMapper.lambdaQuery()
                .eq(SystemConfig::getConfigKey, request.getKey())
                .one();
        if (c == null) {
            c = new SystemConfig(null, request.getKey(), request.getValue(), null, null);
            baseMapper.insert(c);
        } else {
            c.setConfigValue(request.getValue());
            baseMapper.updateById(c);
        }
    }

    @Transactional
    public void setConfigs(Map<String, String> configs) {
        requirePermission(requireLoginUser().isWorker());
        // 已存在 key
        Map<String, SystemConfig> query = baseMapper.lambdaQuery()
                .in(SystemConfig::getConfigKey, configs.keySet())
                .group(SystemConfig::getConfigKey);
        query.forEach((k, v) -> v.setConfigValue(configs.get(k)));
        updateBatchById(query.values());
        // 不存在 key
        List<SystemConfig> other = configs.entrySet().stream()
                .filter(e -> !query.containsKey(e.getKey()))
                .map(e -> new SystemConfig(null, e.getKey(), e.getValue(), null, null))
                .toList();
        saveBatch(other);
    }

    public boolean isAiEnabled() {
        return "true".equals(getConfig("ai.enabled"));
    }

    public String getApiEndpoint() {
        return getConfig("ai.endpoint");
    }

    public String getModel() {
        return getConfig("ai.model");
    }

    public String getApiKey() {
        return getConfig("ai.key");
    }

    public boolean isMultimodalEnabled() {
        return "true".equals(getConfig("ai.multimodal"));
    }

    public long getDailyTokenLimit() {
        String v = getConfig("ai.daily_token_limit");
        if (v == null) return 1_000_000;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return 1_000_000; }
    }

    public String getAiVersion() {
        String v = getConfig("ai.version");
        return v == null ? "1" : v;
    }

    @Transactional
    public void incrementAiVersion() {
        int v = Integer.parseInt(getAiVersion());
        ConfigPropertyRequest request = new ConfigPropertyRequest();
        request.setKey("ai.version");
        request.setValue(String.valueOf(v + 1));
        setConfig(request);
    }
}
