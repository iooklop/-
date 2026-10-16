package com.resourceshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "资源审核请求")
public class ReviewRequest {

    @Schema(description = "审核结果", example = "APPROVED", allowableValues = {"APPROVED", "REJECTED"})
    private String status;

    @Schema(description = "驳回原因（驳回时填写）")
    private String reason;
}
