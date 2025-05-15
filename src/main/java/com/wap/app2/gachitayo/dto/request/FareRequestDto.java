package com.wap.app2.gachitayo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FareRequestDto {
    @JsonProperty("stopover_id")
    private Long stopoverId;
    private Integer fare;
}
