package com.studycompanion.service;

import com.studycompanion.entity.User;
import com.studycompanion.mapper.UserMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.JwtUtil;
import com.studycompanion.dto.LoginRequest;
import com.studycompanion.dto.RegisterRequest;
import com.studycompanion.service.impl.UserServiceImpl;
import com.studycompanion.vo.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword(new BCryptPasswordEncoder().encode("password123"));
        testUser.setNickname("测试用户");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("测试用户");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void register_Success() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(jwtUtil.generateToken(any(), any())).thenReturn("jwt-token");

        LoginResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        verify(userMapper, times(1)).insert(any(User.class));
        verify(subjectMapper, times(3)).insert(any()); // 3 preset subjects
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });
    }

    @Test
    void login_Success() {
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(jwtUtil.generateToken(any(), any())).thenReturn("jwt-token");

        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            userService.login(loginRequest);
        });
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        loginRequest.setPassword("wrongpassword");
        when(userMapper.selectOne(any())).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> {
            userService.login(loginRequest);
        });
    }

    @Test
    void getProfile_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        var profile = userService.getProfile(1L);

        assertNotNull(profile);
        assertEquals("test@example.com", profile.getEmail());
        assertEquals("测试用户", profile.getNickname());
    }

    @Test
    void getProfile_UserNotFound_ThrowsException() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            userService.getProfile(999L);
        });
    }
}
