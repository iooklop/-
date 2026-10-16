package com.resourceshare.controller;

import com.resourceshare.common.Result;
import com.resourceshare.service.CurrentUserService;
import com.resourceshare.service.LikeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/likes")
@CrossOrigin(origins = "*")
public class LikeController {

    private final LikeService likeService;
    private final CurrentUserService currentUserService;

    public LikeController(LikeService likeService, CurrentUserService currentUserService) {
        this.likeService = likeService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/{resourceId}/toggle")
    public Result toggle(@PathVariable Integer resourceId) {
        return Result.success(likeService.toggle(currentUserService.getCurrentUserId(), resourceId));
    }

    @GetMapping("/check/{resourceId}")
    public Result check(@PathVariable Integer resourceId) {
        return Result.success(likeService.isLiked(currentUserService.getCurrentUserId(), resourceId));
    }
}
