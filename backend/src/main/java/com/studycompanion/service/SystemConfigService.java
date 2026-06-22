package com.studycompanion.service;

import java.util.Map;

public interface SystemConfigService {
    String getConfig(String key);
    String getConfig(String key, String defaultValue);
    void updateConfig(String key, String value);
    Map<String, String> getAllConfigs();
    void updateConfigs(Map<String, String> configs);
}
