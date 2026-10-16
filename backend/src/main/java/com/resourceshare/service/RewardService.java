package com.resourceshare.service;

import com.resourceshare.entity.Reward;
import com.resourceshare.entity.Resource;
import com.resourceshare.exception.CustomException;
import com.resourceshare.repository.RewardRepository;
import com.resourceshare.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;
    private final ResourceRepository resourceRepository;
    private final WechatPayService wechatPayService;

    public RewardService(RewardRepository rewardRepository,
                         ResourceRepository resourceRepository,
                         WechatPayService wechatPayService) {
        this.rewardRepository = rewardRepository;
        this.resourceRepository = resourceRepository;
        this.wechatPayService = wechatPayService;
    }

    public Map<String, Object> create(Integer userId, Integer resourceId, BigDecimal amount) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new CustomException("资源不存在"));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("打赏金额必须大于0");
        }
        String orderNo = "REWARD" + System.currentTimeMillis() + (userId % 1000);
        Reward reward = new Reward();
        reward.setOrderNo(orderNo);
        reward.setUserId(userId);
        reward.setResourceId(resourceId);
        reward.setUploaderId(resource.getUploaderId());
        reward.setAmount(amount);
        reward.setStatus("PENDING");
        rewardRepository.save(reward);

        Map<String, Object> pay = wechatPayService.createNativePay(orderNo, "打赏：" + resource.getTitle(), amount);
        pay.put("rewardId", reward.getId());
        return pay;
    }

    public void mockPay(String orderNo) {
        markPaid(orderNo);
    }

    public void markPaid(String orderNo) {
        Reward reward = rewardRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new CustomException("订单不存在"));
        if (!"PAID".equals(reward.getStatus())) {
            reward.setStatus("PAID");
            reward.setPayTime(LocalDateTime.now());
            rewardRepository.save(reward);
        }
    }

    public List<Reward> myRewards(Integer userId) {
        return rewardRepository.findByUserIdOrderByIdDesc(userId);
    }

    public List<Reward> myReceived(Integer uploaderId) {
        return rewardRepository.findByUploaderIdOrderByIdDesc(uploaderId);
    }
}
