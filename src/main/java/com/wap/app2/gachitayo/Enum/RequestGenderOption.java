package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum RequestGenderOption {
    MIXED, ONLY;

    @JsonCreator
    public static RequestGenderOption fromString(String option) {
        return Arrays.stream(RequestGenderOption.values())
                .filter(rg -> rg.name().equalsIgnoreCase(option))
                .findAny()
                .orElse(null);
    }
}
