package com.wap.app2.gachitayo.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopoverUpdateDto {
    @JsonProperty("stopover_id")
    private Long stopoverId;
    @JsonProperty("member_id")
    private Long memberId;
    private LocationDto location;
    @JsonProperty("stopover_type")
    private LocationType stopoverType;
}
