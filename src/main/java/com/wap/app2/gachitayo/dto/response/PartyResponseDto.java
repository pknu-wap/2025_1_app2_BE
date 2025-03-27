package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.GenderOption;
import lombok.Builder;

import java.util.List;

@Builder
public record PartyResponseDto(
        @JsonProperty("party_id")
        Long id,
        @JsonProperty("party_stopovers")
        List<StopoverResponseDto> stopovers,
        @JsonProperty("party_max_person")
        Integer maxPerson,
        @JsonProperty("party_option")
        GenderOption genderOption
) {
}
