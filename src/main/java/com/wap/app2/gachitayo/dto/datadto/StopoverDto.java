package com.wap.app2.gachitayo.dto.datadto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.Enum.validator.ValidEnum;
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
    @ValidEnum(enumClass= LocationType.class, message = "Invalid stopover type")
    private LocationType stopoverType = LocationType.STOPOVER;

    @JsonCreator
    public void setStopoverType(String stopoverType) {
        this.stopoverType = LocationType.fromString(stopoverType);
        if(this.stopoverType == null) {
            this.stopoverType = LocationType.STOPOVER;
        }
    }
}
