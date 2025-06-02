package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.Enum.Gender;
import java.util.List;

public record MemberProfileResponseDto(
        String name,
        String phone,
        int age,
        Gender gender,
        String profileImageUrl,
        String email,
        double review_score,
        List<String> top_tags,
        int total_saved_amount
) {
}
