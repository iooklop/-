package com.resourceshare.controller;

import com.resourceshare.common.Result;
import com.resourceshare.service.CurrentUserService;
import com.resourceshare.service.FavoriteService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final CurrentUserService currentUserService;

    public FavoriteController(FavoriteService favoriteService, CurrentUserService currentUserService) {
        this.favoriteService = favoriteService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Result myFavorites() {
        return Result.success(favoriteService.myFavorites(currentUserService.getCurrentUserId()));
    }

    @PostMapping("/{resourceId}/toggle")
    public Result toggle(@PathVariable Integer resourceId) {
        return Result.success(favoriteService.toggle(currentUserService.getCurrentUserId(), resourceId));
    }

    @GetMapping("/check/{resourceId}")
    public Result check(@PathVariable Integer resourceId) {
        return Result.success(favoriteService.isFavorited(currentUserService.getCurrentUserId(), resourceId));
    }
}
