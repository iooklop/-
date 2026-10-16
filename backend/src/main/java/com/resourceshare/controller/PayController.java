package com.resourceshare.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourceshare.service.RewardService;
import com.resourceshare.service.WechatPayService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@CrossOrigin(origins = "*")
public class PayController {

    private final RewardService rewardService;
    private final WechatPayService wechatPayService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PayController(RewardService rewardService, WechatPayService wechatPayService) {
        this.rewardService = rewardService;
        this.wechatPayService = wechatPayService;
    }

    /**
     * 微信支付异步回调，需配置公网可访问的 notify-url。
     */
    @PostMapping("/wechat/notify")
    public Map<String, Object> wechatNotify(@RequestBody String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode resource = root.path("resource");
            String plain = wechatPayService.decryptNotify(
                    resource.path("ciphertext").asText(),
                    resource.path("nonce").asText(),
                    resource.path("associated_data").asText());
            JsonNode order = objectMapper.readTree(plain);
            String orderNo = order.path("out_trade_no").asText();
            String tradeState = order.path("trade_state").asText();
            if ("SUCCESS".equals(tradeState)) {
                rewardService.markPaid(orderNo);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", "SUCCESS");
            resp.put("message", "成功");
            return resp;
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", "FAIL");
            resp.put("message", e.getMessage());
            return resp;
        }
    }
}
