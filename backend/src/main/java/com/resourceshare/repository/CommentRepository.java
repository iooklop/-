package com.resourceshare.repository;

import com.resourceshare.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Page<Comment> findByResourceId(Integer resourceId, Pageable pageable);

    long countByResourceId(Integer resourceId);
}
