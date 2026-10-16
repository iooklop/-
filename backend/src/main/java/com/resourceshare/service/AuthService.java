package com.resourceshare.service;

import com.resourceshare.entity.User;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.UserRepository;
import com.resourceshare.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("用户不存在"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException("密码错误");
        }
        if ("DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new CustomException("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", user.getRole());
        result.put("user", user);
        return result;
    }

    public User register(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new CustomException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new CustomException("密码不能为空");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new CustomException("用户名已存在");
        }
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()
                && userRepository.existsByPhone(user.getPhone())) {
            throw new CustomException("手机号已被注册");
        }
        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        user.setRole("USER");
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }
}
