package com.resourceshare.service;

import com.resourceshare.entity.DownloadRecord;
import com.resourceshare.entity.Resource;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.DownloadRecordRepository;
import com.resourceshare.repository.ResourceRepository;
import com.resourceshare.repository.UserRepository;
import com.resourceshare.util.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final DownloadRecordRepository downloadRecordRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public ResourceService(ResourceRepository resourceRepository,
                           DownloadRecordRepository downloadRecordRepository,
                           FileStorageService fileStorageService,
                           UserRepository userRepository) {
        this.resourceRepository = resourceRepository;
        this.downloadRecordRepository = downloadRecordRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    public Page<Resource> search(String keyword, Integer categoryId, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        Page<Resource> result = resourceRepository.searchApproved(kw, categoryId, pageable);
        result.getContent().forEach(this::enrichUploaderName);
        return result;
    }

    private Sort resolveSort(String sort) {
        if ("downloads".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "downloadCount");
        }
        if ("views".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "viewCount");
        }
        if ("likes".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "likeCount");
        }
        return Sort.by(Sort.Direction.DESC, "createTime");
    }

    public Resource detail(Integer id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new CustomException("资源不存在"));
        if (!"APPROVED".equals(resource.getStatus())) {
            throw new CustomException("资源不存在或未通过审核");
        }
        resource.setViewCount(resource.getViewCount() + 1);
        Resource saved = resourceRepository.save(resource);
        enrichUploaderName(saved);
        return saved;
    }

    private void enrichUploaderName(Resource resource) {
        if (resource.getUploaderId() != null) {
            userRepository.findById(resource.getUploaderId()).ifPresent(u -> {
                String name = u.getNickname();
                if (name == null || name.trim().isEmpty()) {
                    name = u.getUsername();
                }
                resource.setUploaderName(name);
            });
        }
    }

    public Resource create(Resource resource) {
        resource.setId(null);
        resource.setStatus("PENDING");
        resource.setRejectReason(null);
        return resourceRepository.save(resource);
    }

    public Page<Resource> myUploads(Integer userId, int page, int size) {
        return resourceRepository.findByUploaderId(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
    }

    public Resource update(Integer id, Integer userId, boolean isAdmin, Resource updated) {
        Resource existing = resourceRepository.findById(id)
                .orElseThrow(() -> new CustomException("资源不存在"));
        if (!isAdmin && !existing.getUploaderId().equals(userId)) {
            throw new CustomException("无权修改该资源");
        }
        if (updated.getTitle() != null) {
            existing.setTitle(updated.getTitle());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }
        if (updated.getCategoryId() != null) {
            existing.setCategoryId(updated.getCategoryId());
        }
        if (updated.getPoints() != null) {
            existing.setPoints(updated.getPoints());
        }
        if (updated.getCoverImage() != null) {
            existing.setCoverImage(updated.getCoverImage());
        }
        if (updated.getFileName() != null) {
            existing.setFileName(updated.getFileName());
        }
        if (updated.getFilePath() != null) {
            existing.setFilePath(updated.getFilePath());
        }
        if (updated.getFileSize() != null) {
            existing.setFileSize(updated.getFileSize());
        }
        if (updated.getFileType() != null) {
            existing.setFileType(updated.getFileType());
        }
        return resourceRepository.save(existing);
    }

    public void delete(Integer id, Integer userId, boolean isAdmin) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new CustomException("资源不存在"));
        if (!isAdmin && !resource.getUploaderId().equals(userId)) {
            throw new CustomException("无权删除该资源");
        }
        fileStorageService.delete(resource.getCoverImage());
        fileStorageService.delete(resource.getFilePath());
        resourceRepository.deleteById(id);
    }

    public Resource review(Integer id, String status, String reason) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new CustomException("资源不存在"));
        if ("APPROVED".equals(status)) {
            resource.setStatus("APPROVED");
            resource.setRejectReason(null);
        } else if ("REJECTED".equals(status)) {
            resource.setStatus("REJECTED");
            resource.setRejectReason(reason);
        } else {
            throw new CustomException("非法的审核状态");
        }
        return resourceRepository.save(resource);
    }

    public Resource recordDownload(Integer id, Integer userId) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new CustomException("资源不存在"));
        if (!"APPROVED".equals(resource.getStatus())) {
            throw new CustomException("资源不存在或未通过审核");
        }
        if (resource.getFilePath() == null || resource.getFilePath().isEmpty()) {
            throw new CustomException("该资源暂无可下载文件（示例数据）");
        }
        resource.setDownloadCount(resource.getDownloadCount() + 1);
        resourceRepository.save(resource);
        DownloadRecord record = new DownloadRecord();
        record.setUserId(userId);
        record.setResourceId(id);
        downloadRecordRepository.save(record);
        return resource;
    }

    public Map<String, Object> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", resourceRepository.count());
        stats.put("pending", resourceRepository.countByStatus("PENDING"));
        stats.put("approved", resourceRepository.countByStatus("APPROVED"));
        stats.put("rejected", resourceRepository.countByStatus("REJECTED"));
        return stats;
    }
}
