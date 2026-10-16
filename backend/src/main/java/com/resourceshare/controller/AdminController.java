package com.resourceshare.controller;

import com.resourceshare.common.Result;
import com.resourceshare.dto.ReviewRequest;
import com.resourceshare.entity.Category;
import com.resourceshare.entity.Resource;
import com.resourceshare.entity.User;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.ResourceRepository;
import com.resourceshare.repository.UserRepository;
import com.resourceshare.service.CategoryService;
import com.resourceshare.service.ResourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ResourceService resourceService;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public AdminController(ResourceService resourceService,
                           ResourceRepository resourceRepository,
                           UserRepository userRepository,
                           CategoryService categoryService) {
        this.resourceService = resourceService;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    @GetMapping("/resources")
    public Result resources(@RequestParam(defaultValue = "PENDING") String status,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Resource> result;
        if ("ALL".equals(status)) {
            result = resourceRepository.findAll(pageable);
        } else {
            result = resourceRepository.findByStatus(status, pageable);
        }
        return Result.success(result);
    }

    @PutMapping("/resources/{id}/review")
    public Result review(@PathVariable Integer id, @RequestBody ReviewRequest request) {
        return Result.success(resourceService.review(id, request.getStatus(), request.getReason()));
    }

    @GetMapping("/users")
    public Result users(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return Result.success(userRepository.findAll(pageable));
    }

    @PutMapping("/users/{id}/status")
    public Result updateUserStatus(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id).orElseThrow(() -> new CustomException("用户不存在"));
        user.setStatus(request.get("status"));
        return Result.success(userRepository.save(user));
    }

    @GetMapping("/categories")
    public Result categories() {
        return Result.success(categoryService.listAll());
    }

    @PostMapping("/categories")
    public Result createCategory(@RequestBody Category category) {
        return Result.success(categoryService.create(category));
    }

    @PutMapping("/categories/{id}")
    public Result updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        return Result.success(categoryService.update(id, category));
    }

    @DeleteMapping("/categories/{id}")
    public Result deleteCategory(@PathVariable Integer id) {
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userRepository.count());
        stats.put("resourceCount", resourceRepository.count());
        stats.put("pendingCount", resourceRepository.countByStatus("PENDING"));
        stats.put("approvedCount", resourceRepository.countByStatus("APPROVED"));
        stats.put("rejectedCount", resourceRepository.countByStatus("REJECTED"));
        stats.put("totalDownloads", resourceRepository.sumDownloadCount());
        stats.put("totalViews", resourceRepository.sumViewCount());
        return Result.success(stats);
    }
}
