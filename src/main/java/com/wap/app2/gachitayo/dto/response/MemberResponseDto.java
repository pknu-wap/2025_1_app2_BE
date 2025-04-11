package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.Gender;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberResponseDto(
        @JsonProperty("member_id")
        Long id,
        String name,
        String email,
        String phone,
        Gender gender,
        @JsonProperty("profile_image")
        String profileImage
) {
}
