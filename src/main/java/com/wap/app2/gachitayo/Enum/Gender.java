package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    MALE, FEMALE;

    @JsonCreator
    public static Gender fromString(String gender) {
        for(Gender g : Gender.values()) {
            if(g.name().equals(gender)) {
                return g;
            }
        }
        return null;
    }
}
