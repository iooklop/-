package com.resourceshare.repository;

import com.resourceshare.entity.ResourceLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceLikeRepository extends JpaRepository<ResourceLike, Integer> {
    boolean existsByUserIdAndResourceId(Integer userId, Integer resourceId);

    Optional<ResourceLike> findByUserIdAndResourceId(Integer userId, Integer resourceId);

    Page<ResourceLike> findByUserId(Integer userId, Pageable pageable);

    List<ResourceLike> findByUserIdOrderByIdDesc(Integer userId);

    long countByResourceId(Integer resourceId);
}
