package com.resourceshare.service;

import com.resourceshare.entity.Resource;
import com.resourceshare.entity.ResourceLike;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.ResourceLikeRepository;
import com.resourceshare.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LikeService {

    private final ResourceLikeRepository likeRepository;
    private final ResourceRepository resourceRepository;

    public LikeService(ResourceLikeRepository likeRepository, ResourceRepository resourceRepository) {
        this.likeRepository = likeRepository;
        this.resourceRepository = resourceRepository;
    }

    public Map<String, Object> toggle(Integer userId, Integer resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new CustomException("资源不存在"));
        Optional<ResourceLike> existing = likeRepository.findByUserIdAndResourceId(userId, resourceId);
        Map<String, Object> result = new HashMap<>();
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            resource.setLikeCount(Math.max(0, resource.getLikeCount() - 1));
            result.put("liked", false);
        } else {
            ResourceLike like = new ResourceLike();
            like.setUserId(userId);
            like.setResourceId(resourceId);
            likeRepository.save(like);
            resource.setLikeCount(resource.getLikeCount() + 1);
            result.put("liked", true);
        }
        resourceRepository.save(resource);
        result.put("likeCount", resource.getLikeCount());
        return result;
    }

    public boolean isLiked(Integer userId, Integer resourceId) {
        return likeRepository.existsByUserIdAndResourceId(userId, resourceId);
    }
}
