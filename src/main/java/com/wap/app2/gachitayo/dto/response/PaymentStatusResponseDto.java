package com.wap.app2.gachitayo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PaymentStatusResponseDto(
        @JsonProperty("stopover_id")
        Long stopoverId,
        @JsonProperty("base_fare")
        Integer baseFare,
        @JsonProperty("final_fare")
        Integer finalFare,
        @JsonProperty("is_paid")
        Boolean isPaid
) {
}
