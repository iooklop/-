package com.resourceshare.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Cloudinary 云端存储配置。
 * 仅当 app.cloudinary.enabled=true 时生效，通过环境变量 CLOUDINARY_URL 注入凭证。
 * CLOUDINARY_URL 形如：cloudinary://API_KEY:API_SECRET@CLOUD_NAME
 */
@Configuration
public class CloudinaryConfig {

    @Value("${CLOUDINARY_URL:}")
    private String cloudinaryUrl;

    @Bean
    @ConditionalOnProperty(name = "app.cloudinary.enabled", havingValue = "true")
    public Cloudinary cloudinary() {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new IllegalStateException("app.cloudinary.enabled=true 但未配置 CLOUDINARY_URL 环境变量");
        }
        try {
            URI uri = new URI(cloudinaryUrl);
            String cloudName = uri.getHost();
            String[] credentials = uri.getUserInfo().split(":");
            if (cloudName == null || credentials.length != 2) {
                throw new IllegalStateException("CLOUDINARY_URL 格式应为 cloudinary://API_KEY:API_SECRET@CLOUD_NAME");
            }
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", credentials[0],
                    "api_secret", credentials[1]));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("CLOUDINARY_URL 解析失败：" + cloudinaryUrl, e);
        }
    }
}
