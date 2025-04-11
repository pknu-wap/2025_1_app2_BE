package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GenderOption {
    MIXED, ONLY_MALE, ONLY_FEMALE;

    @JsonCreator
    public static GenderOption fromStringGenderOption(String gender) {
        for (GenderOption genderOption : GenderOption.values()) {
            if (genderOption.name().equalsIgnoreCase(gender)) {
                return genderOption;
            }
        }
        return null;
    }
}
