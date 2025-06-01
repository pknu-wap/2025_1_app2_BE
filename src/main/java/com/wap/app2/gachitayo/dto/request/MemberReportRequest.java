package com.wap.app2.gachitayo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberReportRequest(
        @NotNull
        String email,
        @NotNull
        @Size(max = 500)
        String content
) {
}
