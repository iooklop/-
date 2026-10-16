package com.resourceshare.service;

import com.resourceshare.entity.Comment;
import com.resourceshare.entity.Resource;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.CommentRepository;
import com.resourceshare.repository.ResourceRepository;
import com.resourceshare.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          ResourceRepository resourceRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    public Page<Comment> listByResource(Integer resourceId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Comment> result = commentRepository.findByResourceId(resourceId, pageable);
        result.getContent().forEach(this::enrichUsername);
        return result;
    }

    public Comment add(Integer resourceId, Integer userId, String content, Integer parentId) {
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException("评论内容不能为空");
        }
        resourceRepository.findById(resourceId).orElseThrow(() -> new CustomException("资源不存在"));
        Comment comment = new Comment();
        comment.setResourceId(resourceId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId == null ? 0 : parentId);
        Comment saved = commentRepository.save(comment);
        enrichUsername(saved);
        updateCommentCount(resourceId);
        return saved;
    }

    public void delete(Integer id, Integer userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException("评论不存在"));
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new CustomException("无权删除该评论");
        }
        commentRepository.deleteById(id);
        updateCommentCount(comment.getResourceId());
    }

    private void enrichUsername(Comment comment) {
        userRepository.findById(comment.getUserId()).ifPresent(u -> {
            String name = u.getNickname();
            if (name == null || name.trim().isEmpty()) {
                name = u.getUsername();
            }
            comment.setUsername(name);
        });
    }

    private void updateCommentCount(Integer resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource != null) {
            resource.setCommentCount((int) commentRepository.countByResourceId(resourceId));
            resourceRepository.save(resource);
        }
    }
}
