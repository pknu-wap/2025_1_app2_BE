package com.wap.app2.gachitayo.dto.datadto;

import com.wap.app2.gachitayo.Enum.LocationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StopoverDto {
    LocationDto location;
    LocationType stopoverType;
}
