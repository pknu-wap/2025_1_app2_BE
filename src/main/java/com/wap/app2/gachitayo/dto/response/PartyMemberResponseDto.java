package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.AdditionalRole;
import com.wap.app2.gachitayo.Enum.Gender;
import com.wap.app2.gachitayo.Enum.PartyMemberRole;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record PartyMemberResponseDto(
        Long id,
        String name,
        String email,
        Gender gender,
        PartyMemberRole role,
        @JsonProperty("additional_role")
        AdditionalRole additionalRole
) {
}
