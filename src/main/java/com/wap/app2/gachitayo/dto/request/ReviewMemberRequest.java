package com.wap.app2.gachitayo.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record ReviewMemberRequest(
        @NotBlank
        Long party_id,

        @NotNull
        @DecimalMax("5.0")
        @DecimalMin("0.5")
        double score,

        @NotNull
        Set<String> tags
) {
}
