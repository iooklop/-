package com.resourceshare.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.resourceshare.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Cloudinary 为可选依赖：启用时上传到云端，未启用时回落本地磁盘（开发环境）。
     */
    @Autowired(required = false)
    private Cloudinary cloudinary;

    /**
     * 保存上传的文件。
     * 云端模式返回 https 地址；本地模式返回 /uploads/{subDir}/{uuid.ext}
     */
    public String storeFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (cloudinary != null) {
            return storeToCloudinary(file, subDir);
        }
        return storeToLocal(file, subDir);
    }

    private String storeToCloudinary(MultipartFile file, String subDir) {
        File temp = null;
        try {
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String publicId = UUID.randomUUID().toString().replace("-", "");
            temp = File.createTempFile("upload-", (ext != null && !ext.isEmpty() ? "." + ext : ""));
            file.transferTo(temp);
            // files 目录存任意文件（PDF/压缩包等）用 raw；封面/头像用 auto（图片）
            String resourceType = "files".equals(subDir) ? "raw" : "auto";
            Map<?, ?> result = cloudinary.uploader().upload(temp, ObjectUtils.asMap(
                    "folder", subDir,
                    "public_id", publicId,
                    "resource_type", resourceType,
                    "overwrite", false));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new CustomException("文件上传到云端失败：" + e.getMessage());
        } finally {
            if (temp != null) {
                temp.delete();
            }
        }
    }

    private String storeToLocal(MultipartFile file, String subDir) {
        try {
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString().replace("-", "")
                    + (ext != null && !ext.isEmpty() ? "." + ext : "");
            Path dir = Paths.get(uploadDir, subDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new CustomException("文件保存失败：" + e.getMessage());
        }
    }

    /**
     * 根据存储路径加载文件。云端地址（http/https）走 UrlResource，本地 /uploads/... 走本地文件
     */
    public Resource loadAsResource(String path) {
        if (path == null) {
            throw new CustomException("非法的文件路径");
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            try {
                return new UrlResource(new URL(path));
            } catch (MalformedURLException e) {
                throw new CustomException("非法的文件地址：" + path);
            }
        }
        if (!path.startsWith("/uploads/")) {
            throw new CustomException("非法的文件路径");
        }
        String local = path.substring("/uploads/".length());
        Path file = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(local).normalize();
        Resource resource = new FileSystemResource(file);
        if (!resource.exists()) {
            throw new CustomException("文件不存在或已被删除");
        }
        return resource;
    }

    public void delete(String path) {
        if (path == null) {
            return;
        }
        // 云端文件暂不主动删除（避免误删与 public_id 解析复杂度），仅清理本地文件
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return;
        }
        if (!path.startsWith("/uploads/")) {
            return;
        }
        try {
            String local = path.substring("/uploads/".length());
            Path file = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(local).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // 删除失败不影响主流程
        }
    }
}
