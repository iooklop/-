package com.resourceshare.controller;

import com.resourceshare.common.Result;
import com.resourceshare.dto.RewardRequest;
import com.resourceshare.service.CurrentUserService;
import com.resourceshare.service.RewardService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewards")
@CrossOrigin(origins = "*")
public class RewardController {

    private final RewardService rewardService;
    private final CurrentUserService currentUserService;

    public RewardController(RewardService rewardService, CurrentUserService currentUserService) {
        this.rewardService = rewardService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public Result create(@RequestBody RewardRequest request) {
        return Result.success(rewardService.create(
                currentUserService.getCurrentUserId(), request.getResourceId(), request.getAmount()));
    }

    @PostMapping("/{orderNo}/mock-pay")
    public Result mockPay(@PathVariable String orderNo) {
        rewardService.mockPay(orderNo);
        return Result.success("模拟支付成功");
    }

    @GetMapping("/mine")
    public Result mine() {
        return Result.success(rewardService.myRewards(currentUserService.getCurrentUserId()));
    }

    @GetMapping("/received")
    public Result received() {
        return Result.success(rewardService.myReceived(currentUserService.getCurrentUserId()));
    }
}
