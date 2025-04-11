package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.Enum.Gender;
import com.wap.app2.gachitayo.Enum.MemberRole;
import lombok.Builder;

@Builder
public record PartyMemberResponseDto(
        Long id,
        String name,
        String email,
        Gender gender,
        MemberRole role
) {
}
