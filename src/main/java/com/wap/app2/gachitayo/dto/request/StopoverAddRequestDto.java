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
    @JsonProperty("member_email")
    private String memberEmail;
    private LocationDto location;
}
