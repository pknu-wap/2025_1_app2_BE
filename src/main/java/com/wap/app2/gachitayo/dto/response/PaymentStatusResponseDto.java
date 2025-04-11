package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.dto.datadto.PartyMemberDto;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import lombok.Builder;

@Builder
public record PaymentStatusResponseDto(
        @JsonProperty("party_member")
        PartyMemberDto member,
        @JsonProperty("stopover")
        StopoverDto stopoverDto,
        @JsonProperty("is_paid")
        boolean isPaid,
        @JsonProperty("fare_to_pay")
        Integer fareToPay
) {
}