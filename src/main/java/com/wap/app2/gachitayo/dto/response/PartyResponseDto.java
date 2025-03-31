package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import lombok.Builder;

import java.util.List;

@Builder
public record PartyResponseDto(
        @JsonProperty("party_id")
        Long id,
        @JsonProperty("party_stopovers")
        List<StopoverDto> stopovers,
        @JsonProperty("party_radius")
        Integer radius,
        @JsonProperty("party_max_person")
        Integer maxPerson,
        @JsonProperty("party_option")
        GenderOption genderOption
) {
}
