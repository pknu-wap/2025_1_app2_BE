package com.wap.app2.gachitayo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyJoinRequestSocketRequestDto {
    @JsonProperty("request_id")
    private Long requestId;
}
