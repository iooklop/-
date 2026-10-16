package com.resourceshare.controller;

import com.resourceshare.common.Result;
import com.resourceshare.dto.LoginRequest;
import com.resourceshare.entity.User;
import com.resourceshare.service.AuthService;
import com.resourceshare.util.FileStorageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final FileStorageService fileStorageService;

    public AuthController(AuthService authService, FileStorageService fileStorageService) {
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/register")
    public Result register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String nickname,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) Integer age,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) MultipartFile avatar) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setName(name);
        user.setAge(age);
        user.setPhone(phone);
        user.setEmail(email);
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(fileStorageService.storeFile(avatar, "avatars"));
        }
        return Result.success(authService.register(user));
    }
}
