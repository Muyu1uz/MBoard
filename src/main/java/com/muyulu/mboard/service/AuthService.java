package com.muyulu.mboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyulu.mboard.common.config.JwtTokenProvider;
import com.muyulu.mboard.common.exception.BusinessException;
import com.muyulu.mboard.entity.User;
import com.muyulu.mboard.enums.RoleType;
import com.muyulu.mboard.mapper.UserMapper;
import com.muyulu.mboard.service.dto.LoginRequest;
import com.muyulu.mboard.service.dto.RegisterRequest;
import com.muyulu.mboard.view.AuthView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthView register(RegisterRequest request) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (existing != null) {
            throw new BusinessException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleType.USER);
        userMapper.insert(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return AuthView.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .token(token)
                .build();
    }

    public AuthView login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid username or password");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return AuthView.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .token(token)
                .build();
    }
}
