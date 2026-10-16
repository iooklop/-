package com.resourceshare.controller;

import com.resourceshare.common.Result;
import com.resourceshare.entity.Resource;
import com.resourceshare.entity.User;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.DownloadRecordRepository;
import com.resourceshare.repository.ResourceRepository;
import com.resourceshare.repository.UserRepository;
import com.resourceshare.service.CurrentUserService;
import com.resourceshare.service.ResourceService;
import com.resourceshare.util.FileStorageService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final DownloadRecordRepository downloadRecordRepository;
    private final ResourceService resourceService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserController(CurrentUserService currentUserService,
                          UserRepository userRepository,
                          ResourceRepository resourceRepository,
                          DownloadRecordRepository downloadRecordRepository,
                          ResourceService resourceService,
                          BCryptPasswordEncoder passwordEncoder,
                          FileStorageService fileStorageService) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.downloadRecordRepository = downloadRecordRepository;
        this.resourceService = resourceService;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/me")
    public Result me() {
        return Result.success(currentUserService.getCurrentUser());
    }

    @PutMapping("/me")
    public Result updateMe(@RequestBody User user) {
        User current = currentUserService.getCurrentUser();
        if (user.getNickname() != null) {
            current.setNickname(user.getNickname());
        }
        if (user.getName() != null) {
            current.setName(user.getName());
        }
        if (user.getAvatar() != null) {
            current.setAvatar(user.getAvatar());
        }
        if (user.getPhone() != null) {
            current.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            current.setEmail(user.getEmail());
        }
        if (user.getAge() != null) {
            current.setAge(user.getAge());
        }
        return Result.success(userRepository.save(current));
    }

    @PostMapping("/me/avatar")
    public Result uploadAvatar(@RequestParam("avatar") MultipartFile avatar) {
        String url = fileStorageService.storeFile(avatar, "avatars");
        User current = currentUserService.getCurrentUser();
        current.setAvatar(url);
        return Result.success(userRepository.save(current));
    }

    @PutMapping("/me/password")
    public Result updatePassword(@RequestBody Map<String, String> request) {
        User current = currentUserService.getCurrentUser();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        if (!passwordEncoder.matches(oldPassword, current.getPassword())) {
            throw new CustomException("旧密码错误");
        }
        current.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(current);
        return Result.success("密码修改成功");
    }

    @GetMapping("/me/resources")
    public Result myResources(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(resourceService.myUploads(currentUserService.getCurrentUserId(), page, size));
    }

    @GetMapping("/me/downloads")
    public Result myDownloads() {
        List<Resource> list = downloadRecordRepository
                .findByUserIdOrderByIdDesc(currentUserService.getCurrentUserId()).stream()
                .map(r -> resourceRepository.findById(r.getResourceId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userRepository.count());
        stats.put("resourceCount", resourceRepository.count());
        stats.put("downloadCount", resourceRepository.sumDownloadCount());
        return Result.success(stats);
    }
}
