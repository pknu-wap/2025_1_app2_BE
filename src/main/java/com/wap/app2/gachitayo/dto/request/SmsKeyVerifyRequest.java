package com.wap.app2.gachitayo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SmsKeyVerifyRequest(
        @NotBlank
        String key
) {
}
