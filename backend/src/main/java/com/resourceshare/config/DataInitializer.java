package com.resourceshare.config;

import com.resourceshare.entity.Category;
import com.resourceshare.entity.Resource;
import com.resourceshare.entity.User;
import com.resourceshare.repository.CategoryRepository;
import com.resourceshare.repository.ResourceRepository;
import com.resourceshare.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ResourceRepository resourceRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ResourceRepository resourceRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.resourceRepository = resourceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initUsers();
        initCategories();
        initResources();
    }

    private void initUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setNickname("管理员");
        admin.setName("系统管理员");
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");
        userRepository.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname("测试用户");
        user.setName("张三");
        user.setAge(25);
        user.setRole("USER");
        user.setStatus("ACTIVE");
        userRepository.save(user);
    }

    private void initCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }
        String[] names = {
                "后端开发", "前端开发", "数据库", "人工智能", "大数据",
                "操作系统", "计算机网络", "开发工具", "源码项目", "学习资料", "模板资源", "其他"
        };
        for (int i = 0; i < names.length; i++) {
            Category category = new Category();
            category.setName(names[i]);
            category.setParentId(0);
            category.setSortOrder(i + 1);
            category.setStatus("ACTIVE");
            categoryRepository.save(category);
        }
    }

    private void initResources() {
        if (resourceRepository.count() > 0) {
            return;
        }
        List<Category> categories = categoryRepository.findAll();
        User uploader = userRepository.findByUsername("user").orElse(null);
        if (uploader == null || categories.isEmpty()) {
            return;
        }

        Resource r1 = buildSample("Spring Boot 项目实战完整源码（含数据库脚本）",
                "一套前后端分离的资源共享平台源码，包含用户、资源、审核、收藏、点赞、评论、下载等模块，附带 MySQL 建表脚本与部署说明。",
                categories.get(0).getId(), uploader.getId(), 152, 38, 21, true);
        resourceRepository.save(r1);

        Resource r2 = buildSample("Vue3 + Vite 前端开发视频教程",
                "从零入门 Vue3 组合式 API、Pinia 状态管理、Vue Router 路由与 Vite 工程化，共 30 节视频。",
                categories.get(1).getId(), uploader.getId(), 96, 12, 45, true);
        resourceRepository.save(r2);

        Resource r3 = buildSample("MySQL 性能优化实战笔记 PDF",
                "涵盖索引优化、SQL 改写、慢查询分析、分库分表等实战经验总结，图文并茂。",
                categories.get(2).getId(), uploader.getId(), 210, 55, 18, false);
        resourceRepository.save(r3);

        Resource r4 = buildSample("算法与数据结构面试题合集",
                "整理了 200+ 道常见面试题与详细题解，适合备战校招与社招。",
                categories.get(9).getId(), uploader.getId(), 88, 26, 12, false);
        resourceRepository.save(r4);

        // 一条待审核示例，用于演示后台审核流程
        Resource pending = buildSample("深度学习入门电子书",
                "深入浅出讲解神经网络、CNN、RNN 与 Transformer，适合初学者。",
                categories.get(3).getId(), uploader.getId(), 0, 0, 0, false);
        pending.setStatus("PENDING");
        resourceRepository.save(pending);
    }

    private Resource buildSample(String title, String description, Integer categoryId,
                                 Integer uploaderId, int viewCount, int downloadCount, int likeCount,
                                 boolean recommended) {
        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setCategoryId(categoryId);
        resource.setUploaderId(uploaderId);
        resource.setViewCount(viewCount);
        resource.setDownloadCount(downloadCount);
        resource.setLikeCount(likeCount);
        resource.setFavoriteCount(likeCount / 2);
        resource.setCommentCount(0);
        resource.setPoints(0);
        resource.setStatus("APPROVED");
        resource.setIsRecommended(recommended);
        resource.setCreateTime(LocalDateTime.now());
        return resource;
    }
}
