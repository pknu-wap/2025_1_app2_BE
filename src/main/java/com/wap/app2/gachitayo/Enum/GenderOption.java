package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum GenderOption {
    MIXED, ONLY_MALE, ONLY_FEMALE;

    @JsonCreator
    public static GenderOption fromStringGenderOption(String gender) {
        return Arrays.stream(GenderOption.values())
                .filter(g -> g.name().equalsIgnoreCase(gender))
                .findAny()
                .orElse(null);
    }
}
