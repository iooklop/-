package com.resourceshare.controller;

import com.resourceshare.common.Result;
import com.resourceshare.entity.Resource;
import com.resourceshare.service.CurrentUserService;
import com.resourceshare.service.ResourceService;
import com.resourceshare.util.FileStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceService resourceService;
    private final FileStorageService fileStorageService;
    private final CurrentUserService currentUserService;

    public ResourceController(ResourceService resourceService,
                              FileStorageService fileStorageService,
                              CurrentUserService currentUserService) {
        this.resourceService = resourceService;
        this.fileStorageService = fileStorageService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Result list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(defaultValue = "newest") String sort) {
        return Result.success(resourceService.search(keyword, categoryId, sort, page, size));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Integer id) {
        return Result.success(resourceService.detail(id));
    }

    @PostMapping
    public Result upload(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) Integer categoryId,
                         @RequestParam(required = false) Integer points,
                         @RequestParam(required = false) MultipartFile cover,
                         @RequestParam(required = false) MultipartFile file) {
        Integer userId = currentUserService.getCurrentUserId();
        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setCategoryId(categoryId);
        resource.setPoints(points == null ? 0 : points);
        resource.setUploaderId(userId);
        if (cover != null && !cover.isEmpty()) {
            resource.setCoverImage(fileStorageService.storeFile(cover, "covers"));
        }
        if (file != null && !file.isEmpty()) {
            resource.setFileName(file.getOriginalFilename());
            resource.setFileType(StringUtils.getFilenameExtension(file.getOriginalFilename()));
            resource.setFileSize(file.getSize());
            resource.setFilePath(fileStorageService.storeFile(file, "files"));
        }
        return Result.success(resourceService.create(resource));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody Resource resource) {
        Integer userId = currentUserService.getCurrentUserId();
        boolean isAdmin = currentUserService.isCurrentUserAdmin();
        return Result.success(resourceService.update(id, userId, isAdmin, resource));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        Integer userId = currentUserService.getCurrentUserId();
        boolean isAdmin = currentUserService.isCurrentUserAdmin();
        resourceService.delete(id, userId, isAdmin);
        return Result.success();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Integer id) {
        Integer userId = currentUserService.getCurrentUserId();
        Resource resource = resourceService.recordDownload(id, userId);
        org.springframework.core.io.Resource file = fileStorageService.loadAsResource(resource.getFilePath());
        String fileName = resource.getFileName() == null ? "download" : resource.getFileName();
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
                .body(file);
    }
}
