package com.resourceshare.service;

import com.resourceshare.entity.User;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new CustomException("401", "未登录");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new CustomException("401", "用户不存在"));
    }

    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean isCurrentUserAdmin() {
        return "ADMIN".equals(getCurrentUser().getRole());
    }
}
