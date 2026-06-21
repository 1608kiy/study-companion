package com.studycompanion.service;

import com.studycompanion.dto.ForgotPasswordRequest;
import com.studycompanion.dto.LoginRequest;
import com.studycompanion.dto.RegisterRequest;
import com.studycompanion.dto.ResetPasswordRequest;
import com.studycompanion.dto.UpdateProfileRequest;
import com.studycompanion.vo.LoginResponse;
import com.studycompanion.vo.UserProfileVO;

public interface UserService {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserProfileVO getProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void deleteAccount(Long userId);

    void logout(String token);

    String forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
