package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LocationType {
    START, STOPOVER, DESTINATION;

    @JsonCreator
    public static LocationType fromString(String locationType) {
        for(LocationType type : LocationType.values()) {
            if(type.name().equalsIgnoreCase(locationType)) {
                return type;
            }
        }
        return null;
    }
}
