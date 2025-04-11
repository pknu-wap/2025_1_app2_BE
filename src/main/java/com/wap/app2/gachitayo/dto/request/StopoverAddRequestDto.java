package com.wap.app2.gachitayo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopoverAddRequestDto {
    @JsonProperty("member_id")
    private Long memberId;
    private LocationDto location;
}
