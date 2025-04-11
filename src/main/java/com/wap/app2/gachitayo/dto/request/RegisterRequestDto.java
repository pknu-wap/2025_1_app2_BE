package com.wap.app2.gachitayo.dto.request;

import com.wap.app2.gachitayo.Enum.Gender;

public record RegisterRequestDto(
        String name,
        String phone,
        int age,
        Gender gender,
        String profileImageUrl,
        String idToken,
        String accessToken
) {
}
