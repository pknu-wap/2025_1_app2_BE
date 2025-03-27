package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;

public record PartyResponseDto(
        @JsonProperty("party_id")
        Long id,
        @JsonProperty("party_start")
        LocationDto startLocation,
        @JsonProperty("party_destination")
        LocationDto destination,
        @JsonProperty("party_radius")
        Double radius,
        @JsonProperty("party_max_person")
        Integer maxPerson,
        @JsonProperty("party_option")
        GenderOption genderOption
) {
}
