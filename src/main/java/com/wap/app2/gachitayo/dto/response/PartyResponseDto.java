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
        @JsonProperty("party_members")
        List<PartyMemberResponseDto> members,
        @JsonProperty("party_stopovers")
        List<StopoverDto> stopovers,
        @JsonProperty("party_radius")
        Double radius,
        @JsonProperty("party_max_people")
        Integer maxPeople,
        @JsonProperty("party_current_people")
        Integer currentPeople,
        @JsonProperty("party_option")
        GenderOption genderOption
) {
}
