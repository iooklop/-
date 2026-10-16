package com.resourceshare.repository;

import com.resourceshare.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    boolean existsByUserIdAndResourceId(Integer userId, Integer resourceId);

    Optional<Favorite> findByUserIdAndResourceId(Integer userId, Integer resourceId);

    Page<Favorite> findByUserId(Integer userId, Pageable pageable);

    List<Favorite> findByUserIdOrderByIdDesc(Integer userId);

    long countByResourceId(Integer resourceId);
}
