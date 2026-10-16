package com.resourceshare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourceshare.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WechatPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayService.class);
    private static final String NATIVE_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
    private static final String URL_PATH = "/v3/pay/transactions/native";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.wechat.enabled:false}")
    private boolean enabled;

    @Value("${app.wechat.appid:}")
    private String appid;

    @Value("${app.wechat.mchid:}")
    private String mchid;

    @Value("${app.wechat.serial-no:}")
    private String serialNo;

    @Value("${app.wechat.private-key-path:}")
    private String privateKeyPath;

    @Value("${app.wechat.api-v3-key:}")
    private String apiV3Key;

    @Value("${app.wechat.notify-url:}")
    private String notifyUrl;

    /**
     * 创建 Native 扫码支付订单。
     * 未启用真实支付时返回 mock=true 及模拟二维码内容，便于本地联调。
     */
    public Map<String, Object> createNativePay(String orderNo, String description, BigDecimal amountYuan) {
        if (!enabled) {
            Map<String, Object> result = new HashMap<>();
            result.put("mock", true);
            result.put("orderNo", orderNo);
            result.put("codeUrl", "mockpay://" + orderNo);
            result.put("amount", amountYuan);
            return result;
        }
        try {
            long totalFen = amountYuan.multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP).longValue();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("appid", appid);
            body.put("mchid", mchid);
            body.put("description", description);
            body.put("out_trade_no", orderNo);
            body.put("notify_url", notifyUrl);
            Map<String, Object> amount = new HashMap<>();
            amount.put("total", totalFen);
            amount.put("currency", "CNY");
            body.put("amount", amount);

            String json = objectMapper.writeValueAsString(body);
            String authorization = buildAuthorization("POST", URL_PATH, json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NATIVE_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode node = objectMapper.readTree(response.body());
            if (response.statusCode() == 200 && node.has("code_url")) {
                Map<String, Object> result = new HashMap<>();
                result.put("mock", false);
                result.put("orderNo", orderNo);
                result.put("codeUrl", node.get("code_url").asText());
                result.put("amount", amountYuan);
                return result;
            }
            log.error("微信支付下单失败：{}", response.body());
            throw new CustomException("微信支付下单失败：" + node.path("message").asText("请检查支付配置"));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信支付下单异常", e);
            throw new CustomException("微信支付下单异常：" + e.getMessage());
        }
    }

    /**
     * 解密微信支付回调中的 resource 字段，返回明文 JSON。
     */
    public String decryptNotify(String ciphertext, String nonce, String associatedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            byte[] plain = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CustomException("微信支付回调解密失败：" + e.getMessage());
        }
    }

    private String buildAuthorization(String method, String urlPath, String body) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String message = method + "\n" + urlPath + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
        String signature = sign(message);
        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + mchid + "\",nonce_str=\"" + nonce
                + "\",signature=\"" + signature + "\",timestamp=\"" + timestamp
                + "\",serial_no=\"" + serialNo + "\"";
    }

    private String sign(String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(loadPrivateKey());
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String pem = Files.readString(Paths.get(privateKeyPath));
        String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }
}
