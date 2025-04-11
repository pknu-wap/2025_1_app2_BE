package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import lombok.Builder;

@Builder
public record PartyCreateResponseDto(
        @JsonProperty("party_id")
        Long id,
        @JsonProperty("party_host_name")
        String hostName,
        @JsonProperty("party_host_email")
        String hostEmail,
        @JsonProperty("party_start")
        StopoverDto startLocation,
        @JsonProperty("party_destination")
        StopoverDto destination,
        @JsonProperty("party_radius")
        Double radius,
        @JsonProperty("party_max_person")
        Integer maxPerson,
        @JsonProperty("party_option")
        GenderOption genderOption
) {
}
