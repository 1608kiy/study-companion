package com.studycompanion.service;

import java.util.Map;

public interface AdminService {
    Map<String, Object> getDashboardStats();
    Map<String, Object> getUserList(int page, int size, String keyword);
    void updateUser(Long userId, Map<String, Object> updates);
    void deleteUser(Long userId);
    Map<String, String> getAiConfig();
    void updateAiConfig(Map<String, String> config);
}
