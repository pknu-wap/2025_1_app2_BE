package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RequestGenderOption {
    MIXED, ONLY;

    @JsonCreator
    public static RequestGenderOption fromString(String option) {
        for(RequestGenderOption r : RequestGenderOption.values()) {
            if(r.toString().equals(option)) {
                return r;
            }
        }
        return null;
    }
}
