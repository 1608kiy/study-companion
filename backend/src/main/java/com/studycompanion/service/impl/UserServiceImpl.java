package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.common.JwtUtil;
import com.studycompanion.dto.LoginRequest;
import com.studycompanion.dto.RegisterRequest;
import com.studycompanion.dto.UpdateProfileRequest;
import com.studycompanion.entity.Subject;
import com.studycompanion.entity.User;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.mapper.UserMapper;
import com.studycompanion.service.UserService;
import com.studycompanion.vo.LoginResponse;
import com.studycompanion.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final SubjectMapper subjectMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String PRESET_SUBJECTS_KEY = "preset:subjects";

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getEmail().split("@")[0]);
        userMapper.insert(user);

        // 创建预设科目
        createPresetSubjects(user.getId());

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return new LoginResponse(user.getId(), user.getEmail(), user.getNickname(), token);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return new LoginResponse(user.getId(), user.getEmail(), user.getNickname(), token);
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setDailyGoal(user.getDailyGoal());
        vo.setWeeklyGoal(user.getWeeklyGoal());
        vo.setMonthlyGoal(user.getMonthlyGoal());
        vo.setDarkMode(user.getDarkMode() != null && user.getDarkMode() == 1);
        vo.setNotificationEnabled(user.getNotificationEnabled() == null || user.getNotificationEnabled() == 1);
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getDailyGoal() != null) {
            user.setDailyGoal(request.getDailyGoal());
        }
        if (request.getWeeklyGoal() != null) {
            user.setWeeklyGoal(request.getWeeklyGoal());
        }
        if (request.getMonthlyGoal() != null) {
            user.setMonthlyGoal(request.getMonthlyGoal());
        }
        if (request.getDarkMode() != null) {
            user.setDarkMode(request.getDarkMode() ? 1 : 0);
        }
        if (request.getNotificationEnabled() != null) {
            user.setNotificationEnabled(request.getNotificationEnabled() ? 1 : 0);
        }

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.deleteById(userId);
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + token,
                    String.valueOf(userId),
                    24,
                    TimeUnit.HOURS
            );
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }

    private void createPresetSubjects(Long userId) {
        List<String[]> presets = getPresetSubjects();
        for (int i = 0; i < presets.size(); i++) {
            Subject subject = new Subject();
            subject.setUserId(userId);
            subject.setName(presets.get(i)[0]);
            subject.setColor(presets.get(i)[1]);
            subject.setIcon(presets.get(i)[2]);
            subject.setSortOrder(i + 1);
            subject.setIsPreset(1);
            subjectMapper.insert(subject);
        }
    }

    private List<String[]> getPresetSubjects() {
        List<String[]> presets = new ArrayList<>();
        presets.add(new String[]{"行测", "#409EFF", "icon-xingce"});
        presets.add(new String[]{"申论", "#67C23A", "icon-shenlun"});
        presets.add(new String[]{"公共基础知识", "#E6A23C", "icon-gonggong"});
        return presets;
    }
}
