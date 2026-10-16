package com.resourceshare.service;

import com.resourceshare.entity.Favorite;
import com.resourceshare.entity.Resource;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.FavoriteRepository;
import com.resourceshare.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ResourceRepository resourceRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, ResourceRepository resourceRepository) {
        this.favoriteRepository = favoriteRepository;
        this.resourceRepository = resourceRepository;
    }

    public Map<String, Object> toggle(Integer userId, Integer resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new CustomException("资源不存在"));
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndResourceId(userId, resourceId);
        Map<String, Object> result = new HashMap<>();
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            resource.setFavoriteCount(Math.max(0, resource.getFavoriteCount() - 1));
            result.put("favorited", false);
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setResourceId(resourceId);
            favoriteRepository.save(favorite);
            resource.setFavoriteCount(resource.getFavoriteCount() + 1);
            result.put("favorited", true);
        }
        resourceRepository.save(resource);
        result.put("favoriteCount", resource.getFavoriteCount());
        return result;
    }

    public boolean isFavorited(Integer userId, Integer resourceId) {
        return favoriteRepository.existsByUserIdAndResourceId(userId, resourceId);
    }

    public List<Resource> myFavorites(Integer userId) {
        return favoriteRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(f -> resourceRepository.findById(f.getResourceId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
