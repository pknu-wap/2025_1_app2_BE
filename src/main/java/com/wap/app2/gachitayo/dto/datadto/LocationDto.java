package com.wap.app2.gachitayo.dto.datadto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.LocationType;
import lombok.Data;

@Data
public class LocationDto {
    private String name;
    @JsonProperty("lat")
    private Double latitude;
    @JsonProperty("lng")
    private Double longitude;
    @JsonProperty("location_type")
    private LocationType locationType;
}
