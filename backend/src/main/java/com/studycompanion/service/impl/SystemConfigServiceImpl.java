package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.entity.SystemConfig;
import com.studycompanion.mapper.SystemConfigMapper;
import com.studycompanion.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    
    // 内存缓存，避免频繁查库
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    @Override
    public String getConfig(String key) {
        return getConfig(key, null);
    }

    @Override
    public String getConfig(String key, String defaultValue) {
        // 先查缓存
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }
        
        // 查数据库
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        
        if (config != null && config.getConfigValue() != null) {
            configCache.put(key, config.getConfigValue());
            return config.getConfigValue();
        }
        
        return defaultValue;
    }

    @Override
    public void updateConfig(String key, String value) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        
        if (config != null) {
            config.setConfigValue(value);
            systemConfigMapper.updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            systemConfigMapper.insert(config);
        }
        
        // 更新缓存
        configCache.put(key, value);
        log.info("系统配置已更新: key={}", key);
    }

    @Override
    public Map<String, String> getAllConfigs() {
        // 先从数据库加载所有配置
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        Map<String, String> result = new HashMap<>();
        
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return result;
    }

    @Override
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            updateConfig(entry.getKey(), entry.getValue());
        }
    }
}
