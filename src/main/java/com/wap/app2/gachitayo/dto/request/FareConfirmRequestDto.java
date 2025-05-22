package com.wap.app2.gachitayo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FareConfirmRequestDto {
    @JsonProperty("party_member_id")
    Long partyMemberId;
    @JsonProperty("stopover_id")
    Long stopoverId;
}
