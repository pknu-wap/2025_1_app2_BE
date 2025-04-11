package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record PartyFareAndPaymentStatusResponseDto(
        @JsonProperty("party_id")
        Long partyId,
        @JsonProperty("payment_status")
        List<PaymentStatusResponseDto> paymentStatus
) {
}
