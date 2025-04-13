package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.Enum.Gender;

public record MemberProfileResponseDto(
        String name,
        String phone,
        int age,
        Gender gender,
        String profileImageUrl,
        String email
) {
}
