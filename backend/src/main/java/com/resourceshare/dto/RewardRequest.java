package com.resourceshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "打赏请求")
public class RewardRequest {

    @Schema(description = "被打赏的资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer resourceId;

    @Schema(description = "打赏金额（元）", example = "6.66", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;
}
