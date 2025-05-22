package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record FinalPaymentStatusResponseDto(
        @JsonProperty("party_member_info")
        PartyMemberResponseDto partyMemberInfo,
        @JsonProperty("payment_info")
        PaymentStatusResponseDto paymentStatus
) {
}
