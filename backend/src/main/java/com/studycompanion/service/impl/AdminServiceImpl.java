package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.entity.*;
import com.studycompanion.mapper.*;
import com.studycompanion.service.AdminService;
import com.studycompanion.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final CheckInMapper checkInMapper;
    private final DiaryMapper diaryMapper;
    private final AiChatHistoryMapper aiChatHistoryMapper;
    private final SystemConfigService systemConfigService;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = userMapper.selectCount(null);
        stats.put("totalUsers", totalUsers);
        
        // 今日新增用户
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.ge(User::getCreateTime, today.atStartOfDay());
        long todayNewUsers = userMapper.selectCount(todayUserWrapper);
        stats.put("todayNewUsers", todayNewUsers);
        
        // 总学习时长（使用SQL聚合）
        LambdaQueryWrapper<StudyRecord> studyWrapper = new LambdaQueryWrapper<>();
        List<StudyRecord> allRecords = studyRecordMapper.selectList(studyWrapper);
        int totalDuration = allRecords.stream().mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
        stats.put("totalStudyDuration", totalDuration);
        
        // 今日学习时长
        int todayDuration = allRecords.stream()
                .filter(r -> today.equals(r.getStudyDate()))
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                .sum();
        stats.put("todayStudyDuration", todayDuration);
        
        // 总打卡次数
        long totalCheckIns = checkInMapper.selectCount(null);
        stats.put("totalCheckIns", totalCheckIns);
        
        // 总日记数
        long totalDiaries = diaryMapper.selectCount(null);
        stats.put("totalDiaries", totalDiaries);
        
        // AI对话次数
        long totalAiChats = aiChatHistoryMapper.selectCount(null);
        stats.put("totalAiChats", totalAiChats);
        
        // 最近注册用户
        LambdaQueryWrapper<User> recentUserWrapper = new LambdaQueryWrapper<>();
        recentUserWrapper.orderByDesc(User::getCreateTime).last("LIMIT 5");
        List<User> recentUsers = userMapper.selectList(recentUserWrapper);
        List<Map<String, Object>> recentUserList = recentUsers.stream()
                .map(u -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", u.getId());
                    userMap.put("email", u.getEmail());
                    userMap.put("nickname", u.getNickname());
                    userMap.put("createTime", u.getCreateTime());
                    return userMap;
                })
                .collect(Collectors.toList());
        stats.put("recentUsers", recentUserList);
        
        return stats;
    }

    @Override
    public Map<String, Object> getUserList(int page, int size, String keyword) {
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getEmail, keyword)
                    .or().like(User::getNickname, keyword));
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        // 获取总数
        long total = userMapper.selectCount(wrapper);
        
        // 分页查询
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + size + " OFFSET " + offset);
        List<User> users = userMapper.selectList(wrapper);
        
        List<Map<String, Object>> userList = users.stream()
                .map(u -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", u.getId());
                    userMap.put("email", u.getEmail());
                    userMap.put("nickname", u.getNickname());
                    userMap.put("role", u.getRole());
                    userMap.put("createTime", u.getCreateTime());
                    return userMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", userList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }

    @Override
    @Transactional
    public void updateUser(Long userId, Map<String, Object> updates) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        if (updates.containsKey("nickname")) {
            user.setNickname((String) updates.get("nickname"));
        }
        if (updates.containsKey("role")) {
            user.setRole((String) updates.get("role"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        
        userMapper.updateById(user);
        log.info("管理员更新用户信息: userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 不允许删除管理员
        if ("admin".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        userMapper.deleteById(userId);
        log.info("管理员删除用户: userId={}", userId);
    }

    @Override
    public Map<String, String> getAiConfig() {
        Map<String, String> config = new HashMap<>();
        String apiKey = systemConfigService.getConfig("ai.api-key", "");
        // API Key 脱敏：只显示前4位和后4位
        if (apiKey.length() > 8) {
            config.put("ai.api-key", apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4));
        } else {
            config.put("ai.api-key", apiKey.isEmpty() ? "" : "****");
        }
        config.put("ai.base-url", systemConfigService.getConfig("ai.base-url", ""));
        config.put("ai.model", systemConfigService.getConfig("ai.model", ""));
        config.put("ai.enabled", systemConfigService.getConfig("ai.enabled", "true"));
        return config;
    }

    @Override
    public void updateAiConfig(Map<String, String> config) {
        systemConfigService.updateConfigs(config);
        log.info("管理员更新AI配置");
    }
}
