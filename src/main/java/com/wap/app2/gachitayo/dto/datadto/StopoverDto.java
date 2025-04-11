package com.wap.app2.gachitayo.dto.datadto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.Enum.validator.ValidLocationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopoverDto {
    private Long id;
    private LocationDto location;
    @JsonProperty("stopover_type")
    @ValidLocationType(enumClass= LocationType.class, message = "Invalid stopover type")
    private LocationType stopoverType;
}
