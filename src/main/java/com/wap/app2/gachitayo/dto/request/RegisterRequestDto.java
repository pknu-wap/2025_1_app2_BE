package com.wap.app2.gachitayo.dto.request;

public record RegisterRequestDto(
        String name,
        String phone,
        int age,
        String profileImageUrl,
        String idToken,
        String accessToken
) {
}
