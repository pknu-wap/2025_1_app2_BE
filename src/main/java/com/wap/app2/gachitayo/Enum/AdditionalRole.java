package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum AdditionalRole {
    NONE, BOOKKEEPER;

    @JsonCreator
    public static PartyMemberRole fromString(String additionalRole) {
        return Arrays.stream(PartyMemberRole.values())
                .filter(r -> r.name().equalsIgnoreCase(additionalRole))
                .findAny()
                .orElse(null);
    }
}
