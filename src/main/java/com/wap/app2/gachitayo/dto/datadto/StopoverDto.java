package com.wap.app2.gachitayo.dto.datadto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.Enum.validator.ValidEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StopoverDto {
    LocationDto location;
    LocationType stopoverType;
}
