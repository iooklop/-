package com.resourceshare.controller;

import com.resourceshare.common.Result;
import com.resourceshare.entity.Comment;
import com.resourceshare.service.CommentService;
import com.resourceshare.service.CurrentUserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;
    private final CurrentUserService currentUserService;

    public CommentController(CommentService commentService, CurrentUserService currentUserService) {
        this.commentService = commentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/api/resources/{resourceId}/comments")
    public Result list(@PathVariable Integer resourceId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.listByResource(resourceId, page, size));
    }

    @PostMapping("/api/resources/{resourceId}/comments")
    public Result add(@PathVariable Integer resourceId, @RequestBody Comment comment) {
        return Result.success(commentService.add(resourceId,
                currentUserService.getCurrentUserId(), comment.getContent(), comment.getParentId()));
    }

    @DeleteMapping("/api/comments/{id}")
    public Result delete(@PathVariable Integer id) {
        commentService.delete(id, currentUserService.getCurrentUserId(), currentUserService.isCurrentUserAdmin());
        return Result.success();
    }
}
